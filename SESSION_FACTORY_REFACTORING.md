# Session Factory Refactoring

## Overview

This document describes the refactoring of the session management architecture to break the dependency between core modules and the `hitorro-basedms` module.

## Problem Statement

Previously, modules that needed database session management had to depend directly on `hitorro-basedms`, which created tight coupling and made it difficult to:
- Use session management in modules that shouldn't depend on DMS
- Test modules in isolation
- Provide alternative session implementations

## Solution

The session factory has been refactored using the **Abstract Factory pattern** with **runtime registration**:

### Architecture

```
hitorro-base (abstract layer)
├── BaseSession (abstract class)
├── BaseSessionFactory (abstract factory)
└── SessionException

hitorro-basedms (concrete implementation)
├── DMSSession extends BaseSession
└── DMSSessionFactory extends BaseSessionFactory
    └── Registered at runtime via BaseSessionFactory.setFactory()
```

### Key Classes

#### hitorro-base Module

1. **`com.hitorro.util.typesystem.BaseSession`**
   - Abstract base class defining the session contract
   - Contains all session management methods (getSession, commit, rollback, etc.)
   - No dependencies on persistence framework

2. **`com.hitorro.util.typesystem.BaseSessionFactory`**
   - Abstract factory for creating and managing sessions
   - Uses singleton pattern with static factory registration
   - Provides convenience methods like `closeSession()` and `commitAndCloseSession()`

3. **`com.hitorro.util.typesystem.SessionException`**
   - Exception type for session-related errors

#### hitorro-basedms Module

1. **`com.hitorro.basedms.session.DMSSession`**
   - Concrete implementation of BaseSession
   - Wraps Hibernate session functionality
   - Unchanged from original implementation

2. **`com.hitorro.basedms.session.DMSSessionFactory`**
   - Concrete implementation of BaseSessionFactory
   - Creates and manages DMSSession instances
   - Registers itself with BaseSessionFactory at initialization

### Usage

#### For Application Code

Modules can now use session management without depending on `hitorro-basedms`:

```java
// Get a session using the registered factory
BaseSession session = BaseSessionFactory.getFactory().getSession();

try {
    // Use the session
    session.saveOrUpdate(object);
    session.commit();
} finally {
    BaseSessionFactory.closeSession(session);
}
```

#### For Implementation Modules

The concrete implementation (hitorro-basedms) registers itself during initialization:

```java
// In HibernateService.init()
BaseSessionFactory.setFactory(new DMSSessionFactory());
```

## Benefits

1. **Decoupling**: Core modules no longer depend on `hitorro-basedms`
2. **Testability**: Mock session factories can be easily provided for testing
3. **Flexibility**: Alternative persistence implementations can be plugged in
4. **Runtime Configuration**: Factory is registered at runtime, allowing different configurations
5. **Backward Compatibility**: Existing code continues to work unchanged

## Dependencies

### hitorro-base (provides abstractions)
- Depends on: hitorro-util
- Provides: BaseSession, BaseSessionFactory, SessionException

### hitorro-basedms (provides implementation)
- Depends on: hitorro-base (for abstract classes)
- Provides: DMSSession, DMSSessionFactory
- Registers: DMSSessionFactory at initialization

## Migration Notes

Existing code using `BaseSessionFactory.getFactory()` will continue to work without changes, as long as:
1. `hitorro-basedms` is included in the runtime classpath
2. The `HibernateService` is initialized before session access

## Example: Using Sessions Without DMS Dependency

A module that only needs to read data can now:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.hitorro</groupId>
    <artifactId>hitorro-base</artifactId>
    <version>3.0.0</version>
</dependency>
<!-- No dependency on hitorro-basedms needed -->
```

```java
// Java code
public class MyService {
    public void doWork() {
        // Factory will be the DMSSessionFactory at runtime
        // if hitorro-basedms is available
        BaseSession session = BaseSessionFactory.getFactory().getSession();
        try {
            List<MyObject> results = new ArrayList<>();
            session.getObjects(MyObject.class, "someQuery", results);
            // Process results...
        } finally {
            BaseSessionFactory.closeSession(session);
        }
    }
}
```

## Testing

For unit tests, you can provide a mock factory:

```java
@Before
public void setUp() {
    BaseSessionFactory mockFactory = mock(BaseSessionFactory.class);
    BaseSession mockSession = mock(BaseSession.class);
    when(mockFactory.getSession()).thenReturn(mockSession);
    BaseSessionFactory.setFactory(mockFactory);
}

@After  
public void tearDown() {
    BaseSessionFactory.setFactory(null);
}
```

## Implementation Details

### Thread Safety
- Factory registration is static and should occur once during application initialization
- Session management within the factory is synchronized where appropriate
- Individual sessions are not thread-safe (as per original design)

### Error Handling
- If no factory is registered, `getFactory()` returns null
- Convenience methods like `closeSession()` check for null factory
- Concrete implementations should handle `SessionException` appropriately

## Files Modified/Created

### Created in hitorro-base:
- `src/main/java/com/hitorro/util/typesystem/BaseSession.java`
- `src/main/java/com/hitorro/util/typesystem/BaseSessionFactory.java`
- `src/main/java/com/hitorro/util/typesystem/SessionException.java`

### Modified in hitorro-basedms:
- No changes required! The existing code already follows the pattern.
- `DMSSessionFactory` already extends `BaseSessionFactory<BaseSession>`
- `HibernateService.init()` already calls `BaseSessionFactory.setFactory(new DMSSessionFactory())`

## Future Enhancements

Potential improvements to consider:

1. **Factory Provider SPI**: Use Java ServiceLoader for automatic factory registration
2. **Multiple Factory Support**: Allow named factories for multiple databases
3. **Transaction Manager Integration**: Integrate with JTA or Spring transactions
4. **Session Pooling**: Add session pool management at the factory level
5. **Metrics**: Add factory-level metrics for session creation, lifecycle, etc.
