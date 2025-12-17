# Hitorro Base Component Test Suite

This document describes the comprehensive test suite for the Hitorro Base Component project.

## Test Structure

The test suite is organized into the following categories:

### 1. Language Processing Tests (`com.hitorro.language`)
- **IsoLanguageTest**: Tests for ISO 639 language code handling
  - Language construction with various field combinations
  - Field trimming and validation
  - Stemmer class configuration
  - Penn & Treebank integration

- **SentencesTest**: Tests for sentence segmentation and processing
  - Sentence extraction with provided/computed offsets
  - Sentence caching mechanisms
  - Whitespace trimming and blank filtering
  - Visitor pattern implementation
  - PrintWriter output functionality

- **SnowballSimpleStemmerTest**: Tests for word stemming functionality
  - Basic stemming operations
  - Edge cases (empty strings, single characters)
  - Thread safety verification
  - Special character handling
  - Multiple language support

### 2. Document Processing Tests (`com.hitorro.base.docprocessing.docmap`)
- **PDFDocToBagTest**: Tests for PDF document processing
  - File validation and loading
  - Metadata extraction (title, author, keywords, creation date)
  - Content extraction
  - Document copying and reset operations
  - Source URL generation

### 3. Utility Classes Tests (`com.hitorro.util.objects`)
- **DomainValueTest**: Tests for domain-value pair handling
  - Construction with various inputs
  - Domain and value getters/setters
  - String concatenation and tuple generation
  - Special character and Unicode support
  - Edge cases (null, empty, long strings)

### 4. Network/RPC Tests (`com.hitorro.network.rpc`)
- **RPCMessageTest**: Tests for RPC message handling
  - Endpoint configuration
  - Payload management
  - Status code and message handling
  - Sequence number management
  - Request/response differentiation
  - IPv4/IPv6 address support
  - Handler management

### 5. Integration Tests (`com.hitorro.integration`)
- **LanguageProcessingIntegrationTest**: End-to-end language processing tests
  - Complete language instance creation
  - Multiple language configurations
  - Minimal and partial field handling

- **DocumentProcessingIntegrationTest**: End-to-end document processing tests
  - DocToBag interface implementation
  - Multiple instance creation
  - Document copying workflows
  - State maintenance across operations

## Running the Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=IsoLanguageTest
```

### Run Tests with Coverage
```bash
mvn clean test jacoco:report
```

### Run Integration Tests Only
```bash
mvn test -Dtest=*IntegrationTest
```

## Test Technologies

- **JUnit 5 (Jupiter)**: Main testing framework
  - Version: 5.10.1
  - Includes parameterized tests support

- **Mockito**: Mocking framework
  - Version: 5.8.0
  - Used for isolating units under test

- **AssertJ**: Fluent assertion library
  - Version: 3.24.2
  - Provides readable and maintainable assertions

## Test Coverage

The test suite covers:

### Unit Tests
- ✅ Language processing components (IsoLanguage, Sentences, Stemmer)
- ✅ Document processing (PDF extraction)
- ✅ Utility classes (DomainValue)
- ✅ Network/RPC messaging

### Integration Tests
- ✅ Language processing workflows
- ✅ Document processing workflows

### Test Statistics
- **Total Test Classes**: 8
- **Total Test Methods**: 100+
- **Coverage Areas**:
  - Constructor testing
  - Getter/setter validation
  - Edge case handling
  - Error condition testing
  - Thread safety verification
  - Special character support
  - Null/empty value handling

## Best Practices Implemented

1. **Descriptive Test Names**: Each test has a clear `@DisplayName` annotation
2. **Arrange-Act-Assert Pattern**: Tests follow AAA pattern for clarity
3. **Parameterized Tests**: Common scenarios are tested with multiple inputs
4. **Mock Isolation**: External dependencies are mocked for unit tests
5. **Integration Tests**: Separate integration tests verify component interactions
6. **Edge Case Coverage**: Tests include null, empty, and boundary conditions
7. **Thread Safety**: Concurrent execution tests where applicable

## Adding New Tests

When adding new tests:

1. Create test class in corresponding package under `src/test/java/`
2. Follow naming convention: `ClassNameTest.java`
3. Use `@DisplayName` for test descriptions
4. Include unit tests for all public methods
5. Add integration tests for workflows
6. Update this README with new test coverage

## Continuous Integration

Tests are designed to run in CI/CD pipelines:
- No external dependencies required
- Fast execution (< 5 seconds for unit tests)
- Deterministic results
- Clean setup and teardown

## Test Maintenance

- Review and update tests when modifying source code
- Keep tests independent and isolated
- Maintain high code coverage (target: >80%)
- Regularly run full test suite before commits

## Troubleshooting

### Common Issues

1. **Tests fail on first run**
   - Solution: Run `mvn clean install` to ensure dependencies are resolved

2. **MockitoAnnotations errors**
   - Solution: Ensure `@BeforeEach` initializes mocks with `MockitoAnnotations.openMocks(this)`

3. **File-based tests fail**
   - Solution: Use `@TempDir` for temporary file operations

## Contact

For questions about the test suite, please refer to the project documentation or contact the development team.
