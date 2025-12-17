

%module SwigUtils 

////////////////////////////////////////////////////////////////
// Declarations for interface C file only
%{

#include <fcntl.h>

#include <signal.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <utime.h>
#include <fcntl.h>
#include <grp.h>

// used for statfs

#include <sys/param.h>
#include <sys/mount.h>

#include <sys/resource.h>
#if defined(_POSIX_C_SOURCE)
#include <sys/statfs.h>
#endif


// this isnt right?  We should be getting this externally I would of thought that including all these duded above would of set it!

#define EXCEPTION_CLASS "ht/util/swig/LibSwigException"

//  macros
#undef	MIN
#define MIN(a, b)  (((a) < (b)) ? (a) : (b))

%}

////////////////////////////////////////////////////////////////
// Swig interface helper files
%include "typemaps.i"
%include "arrays_java.i"
%include "various.i"



//
// wrapper for strerror (which returns a const char *)
//
%{
const char *strerror_w (int errnum)
{
    const char *errMsg = strerror (errnum);
    return (errMsg);
}
%}



////////////////////////////////////////////////////////////////
// Define void * as mapped to a byte array. Redefine the input type conversion
// function to always throw an exception if the argument is null.

%apply char *BYTE {void *};

%typemap(in) void * {
  if (!$input) {
    SWIG_JavaThrowException(jenv, SWIG_JavaNullPointerException, "null byte array");
    return $null;
  }
    $1 = (char *) JCALL2(GetByteArrayElements, jenv, $input, 0);
}

////////////////////////////////////////////////////////////////
// Define a byte buffer structure as a return type which can hold
// void* arrays being cast to byte[]

%typemap(jni) ByteBuffer*        "jbyteArray"
%typemap(jtype) ByteBuffer*      "byte[]"
%typemap(jstype) ByteBuffer*     "byte[]"
%typemap(javaout) ByteBuffer*
{
  return $jnicall;
}

%typemap (out) ByteBuffer*
{
    if ( $1->exception != NULL )
    {
        const char* mesg = ($1->exception);

        jclass excep;
        excep = (*jenv)->FindClass(jenv, EXCEPTION_CLASS);
        if (excep)
            (*jenv)->ThrowNew(jenv, excep, mesg);
        else
            SWIG_JavaThrowException( jenv, SWIG_JavaRuntimeException, mesg );

        freeByteBuffer($1);
	    return $null;
    }
    if ( $1->buffer == NULL )
        $result = $null;
    else
        $result = SWIG_JavaArrayOutSchar(jenv, (signed char*)$1->buffer, $1->len);
}

%typemap (newfree) ByteBuffer*
{
    freeByteBuffer($1);
}

////////////////////////////////////////////////////////////////
// Define an int buffer structure as a return type which can hold
// int arrays and their length. This flavor assumes that the buffer
// should always be freed with "free" after generation of the int
// array. The structure itself is usually static and does not have
// any destructors declared.
// In case of an error code in the buffer, an error message
// is deciphered and an error is thrown.

%{
typedef struct {
    const char* (*errmsgfn)(int errnum);
    int* buffer;
    int len;
    int rc;
} IntBuffer;
%}
%typemap(jni) IntBuffer*        "jintArray"
%typemap(jtype) IntBuffer*      "int[]"
%typemap(jstype) IntBuffer*     "int[]"
%typemap(javaout) IntBuffer*
{
  return $jnicall;
}

%typemap (out) IntBuffer*
{
    if ( $1 == NULL )
        $result = $null;
    else if ( $1->rc != 0 )
    {
        jclass excep;
        const char* mesg = ($1->errmsgfn)($1->rc);
        excep = (*jenv)->FindClass(jenv, EXCEPTION_CLASS);
        if (excep)
            (*jenv)->ThrowNew(jenv, excep, mesg);
        else
            SWIG_JavaThrowException( jenv, SWIG_JavaRuntimeException, mesg );
        return $null;
    }
    else if ( $1->buffer == NULL )
    {
        $result = $null;
    }
    else {
        $result = SWIG_JavaArrayOutInt(jenv, $1->buffer, $1->len);
        free($1->buffer);
    }
}




%inline %{
int makeHardLink (const char *oldpath, const char *newpath)
{
	static IntBuffer intbuf = {strerror_w};
	intbuf.rc = link (oldpath, newpath);
	return intbuf.rc;
}
%}

%inline %{
int makeSymboliLink (const char *oldpath, const char *newpath)
{
	static IntBuffer intbuf = {strerror_w};
	intbuf.rc = symlink (oldpath, newpath);
	return intbuf.rc;
}
%}


%inline %{
int unlink (const char *path)
{
	static IntBuffer intbuf = {strerror_w};
	intbuf.rc = unlink (path);
	return intbuf.rc;
}
%}



//
// from statfs structure - man statfs(2)
//
%inline %{
typedef struct {
    long    fsType;
    long    txfrBlockSize;
    long    totalBlocks;
    long    freeBlocks;
    long    availableBlocks;
    long    totalFileNodes;
    long    freeFileNodes;
    long    fsID;
}StatFSData;
%}


//
// from statfs structure - man stat(2)
//
%inline %{
typedef struct {
    long deviceInodeResidesOn;
    long inodesNumber;
    long inodeProtectionMode;
    long numberOfHardlinks;
    long userId;
    long groupId;
    long deviceType;
    long long lastAccessTime;
    long long lastDateModified;
    long long lastFileStatusChange;
    long fileSizeInBytes;
    long blocksAllocatedToFile;
    long optimalIOOpsBlockSize;
    long userDefinedFlags;
    long fileGenerationNumber;
}StatData;
%}

////////////////////////////////////////////////////////////////
// Declarations of "extension" functions to free stuff
%extend StatFSData {
    ~StatFSData ()
    {
        free(self);
    }

}

////////////////////////////////////////////////////////////////
// Declarations of "extension" functions to free stuff
%extend StatData {
    ~StatData ()
    {
        free(self);
    }

}

%typemap (out) StatFSData*
{
    if ( $1 == NULL )
    {
        const char* mesg = strerror (errno);
        jclass excep;
        excep = jenv->FindClass(EXCEPTION_CLASS);
        if (excep)
            jenv->ThrowNew(excep, mesg);
        else
            SWIG_JavaThrowException( jenv, SWIG_JavaRuntimeException, mesg );

	    $result = $null;
    }
    else {
        *(StatFSData **)&$result = $1;
    }
}

%typemap (out) StatData*
{
    if ( $1 == NULL )
    {
        const char* mesg = strerror (errno);
        jclass excep;
        excep = jenv->FindClass(EXCEPTION_CLASS);
        if (excep)
            jenv->ThrowNew(excep, mesg);
        else
            SWIG_JavaThrowException( jenv, SWIG_JavaRuntimeException, mesg );

	    $result = $null;
    }
    else {
        *(StatData **)&$result = $1;
    }
}

%inline %{
StatFSData* statFS (const char *path)
{
    StatFSData *sData = NULL;

    struct statfs buf;
	int rc = statfs (path, &buf);

    if (rc == 0) {
        sData = (StatFSData*)calloc (1, sizeof (StatFSData));
        sData->fsType = buf.f_type;
        sData->txfrBlockSize = buf.f_bsize;
        sData->totalBlocks = buf.f_blocks;
        sData->freeBlocks = buf.f_bfree;
        sData->availableBlocks = buf.f_bavail;
        sData->totalFileNodes = buf.f_files;
        sData->freeFileNodes = buf.f_ffree;
        sData->fsID = buf.f_ffree;
    }

	return sData;
}
%}

%inline %{
StatData* stat (const char *path)
{
    StatData *sData = NULL;

    struct stat buff;
	int rc = stat (path, &buff);

    if (rc == 0) {
        sData = (StatData*)calloc (1, sizeof (StatData));


        sData->deviceInodeResidesOn = buff.st_dev;
        sData->inodesNumber = buff.st_ino;
        sData->inodeProtectionMode = buff.st_mode;
        sData->numberOfHardlinks = buff.st_nlink;
        sData->userId = buff.st_uid;
        sData->groupId = buff.st_gid;
        sData->deviceType = buff.st_rdev;

        #if defined(_DARWIN_C_SOURCE) 
            sData->lastAccessTime =  buff.st_atimespec.tv_sec;
            sData->lastDateModified = buff.st_mtimespec.tv_sec;
            sData->lastFileStatusChange = buff.st_ctimespec.tv_sec;
            sData->userDefinedFlags = buff.st_flags;
            sData->fileGenerationNumber = buff.st_gen;
        #else
            sData->lastAccessTime =  buff.st_atim.tv_sec;
            sData->lastDateModified = buff.st_mtim.tv_sec;
            sData->lastFileStatusChange = buff.st_ctim.tv_sec;
        #endif
        sData->fileSizeInBytes = buff.st_size;
        sData->blocksAllocatedToFile = buff.st_blocks;
        sData->optimalIOOpsBlockSize = buff.st_blksize;

    }

	return sData;
}
%}



//********************************* PROCESSES *******************************************
/**
	Returns pid_t in 32-bit int.
*/
%inline %{
int  getProcessId ()
{
	return ( getpid() );
}
%}

/**
	Returns pid_t in 32-bit int.
*/
%inline %{
int  getParentProcessId ()
{
	return ( getppid() );
}
%}
/**
	 Set new umask, returning prior value.
	 Uses int args despite mode_t being a u_int16 type.
	 @param mask		in C: mode_t mask
	 @return				in C: mode_t
*/
%inline %{
int  setUMask(int mask)
{
	return ( umask( mask ) );
}
%}


/**
	 Returns process  id.

*/

%inline %{
int  getProcessGroupId()
{
	return ( getpgrp() );
}
%}


/**
	 Sets process group id, returning 0 on success, or errno.
	 @param pid		in C: int pid
	 @param pgrp		in C: int pgrp
	 @return				in C: int
*/


%inline %{
int  setProcessGroupId(int processID, int pgrp )
{
	if ( setpgid( processID, pgrp ) )
		return ( errno );

	return ( 0 );
}
%}

%inline %{
int  sendSignal (int processId, int signal)
{
	return kill(processId, signal);
}
%}

//********************************* GROUPS AND USERS *******************************************************


/*
 * Method:    gid
 */
%inline %{
int  getGroupId()
{
	return ( getgid() );
}
%}

/*
 * Method:    egid
 */
%inline %{
int  getEffectiveGroupId()
{
	return ( getegid() );
}
%}


/*
 * Method:    setgid
 */
%inline %{
int  setGroupId(int gid)
{
	if ( setgid( gid ) )
		return ( errno );

	return ( 0 );
}
%}


/*
 * Method:    setegid
 */

%inline %{
int  setEffectiveGroupID(int gid)
{
	if ( setegid( gid ) )
		return ( errno );

	return ( 0 );
}
%}


// ###########

/*
 * Method:    grstart
 */

%inline %{
int  setEngrent()
{
	setgrent();
}
%}


/**
	No args, no returned value.
*/


%inline %{
int  getGrent()
{
	endgrent();
}
%}






//************************ LIMITS *********************************

%inline %{
int  getLimit(int resource, int isHard )
{
	struct rlimit limits;

	if ( getrlimit( resource, &limits ) != 0 )
		return ( -1 );

	if ( isHard )
		return ( (jlong) limits.rlim_max );
	else
		return ( (jlong) limits.rlim_cur );
}
%}

/**
	 Set the soft-limit for the resource, returning 0 on success or errno.
	 @param resource	in C: int resource
	 @param limit			in C: rlim_t limit
	 @return					in C: int
*/



%inline %{
int  setEffectiveLimit (int resource, int isHard, long limit )
{
	struct rlimit limits;

	// Start by getting current hard & soft limits.
	if ( getrlimit( resource, &limits ) != 0 )
		return ( errno );

	if ( isHard )
		limits.rlim_max = limit;
	else
		limits.rlim_cur = limit;

	if ( setrlimit( resource, &limits ) != 0 )
		return ( errno );

	return ( 0 );
}
%}

//************************ NICE **********************************


/**
	 The docs for getpriority() say to clear errno and check it after.
	 I just assume that whatever gets returned is a valid priority/nice level.
	 This is not so bad an assumption, since we're only referring to the current process.
	 @param which		in C: int which
	 @param whom		in C: int whom
	 @return					in C: int
*/

%inline %{
int  getNice (int which, int whom )
{
	return ( getpriority( which, whom ) );
}
%}

/*
 * Class:     glguerin_suitep_imp_macosx_Nice
 * Method:    renice
 * Signature: (III)I
 */

/**
	 Set priority (nice-level) returning 0 on success or non-zero errno.
	 @param which		in C: int which
	 @param whom		in C: int whom
	 @param nice			in C: int nice
	 @return					in C: int
*/

%inline %{
int  setNice (int which, int whom, int nice )
{
	if ( setpriority( which, whom, nice ) )
		return ( errno );

	return ( 0 );
}
%}


//*********************** TO CONVERT *****************************


/*
 * Class:     glguerin_suitep_imp_macosx_GID
 * Method:    grnext
 * Signature: ([B[I)I
 */

/**
	Fill in the next group's info, returning negative on failure, or name-length in byte[].
*/

/*
JNIEXPORT jint JNICALL
Java_glguerin_suitep_imp_macosx_GID_grnext(
	JNIEnv * env,  jclass ignored,
	jbyteArray nameBuf, jintArray idBuf )
{
	struct group * groupPtr;
	int gid;

	groupPtr = getgrent();
	if ( groupPtr == NULL )  // indicates EOF or an undisclosed error
		return ( -1 );

	gid = groupPtr->gr_gid;
	(*env)->SetIntArrayRegion( env, idBuf, 0, 1, (jint *) &gid );

	return ( strToBuffer( groupPtr->gr_name, env, nameBuf ) );
}
*/