/**************************************************************************
* Copyright (c) 2004, 2005, 2006, 2007, 2009, 2010, 2015 by Chris Gray,   *
* /k/ Embedded Java Solutions and KIFFER Ltd. All rights reserved.        *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
*  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,    *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

#include "clazz.h"
#include "checks.h"
#include "constant.h"
#include "exception.h"
#include "loading.h"
#include "threads.h"

/*
 * If NO_FORMAT_CHECKS is defined, no code to check class file format will
 * be generated (leading to a ~4K smaller binary, but less security).
 */
//#define NO_FORMAT_CHECKS

/*
 * If NO_HIERARCHY_CHECKS is defined, no code to check class file format will
 * be generated (leading to a 400 byte smaller binary, but less security).
 */
//#define NO_HIERARCHY_CHECKS

/*
** The maximum depth of a class in the hierarchy. Deliberately exaggerated,
** so that the reallocMem call will normally free up a useful amount of
** memory.
*/
#define MAX_SUPER_CLASSES 256

w_int loadSuperClasses(w_clazz clazz, w_thread thread) {
  w_clazz super;
  w_int   i;
  w_int   n;
  w_int   result = CLASS_LOADING_DID_NOTHING;

#ifndef NO_FORMAT_CHECKS
  if (isNotSet(clazz->flags, CLAZZ_IS_TRUSTED) && !clazz->temp.super_index) {
    throwException(thread, clazzVerifyError, "Class %k has no superclass", clazz);

    return CLASS_LOADING_FAILED;
  }
#endif

  clazz->supers = allocMem(MAX_SUPER_CLASSES * sizeof(w_clazz));
  if (!clazz->supers) {
    return CLASS_LOADING_FAILED;
  }
  super = clazz;
  n = MAX_SUPER_CLASSES;
  for (i = 0; i < MAX_SUPER_CLASSES; ++i) {
    if (!super->temp.super_index) {
      woempa(1, "Reached top of hierarchy after %d class(es): %k\n", i, super);
      n = i;
      break;
    }

#ifndef NO_FORMAT_CHECKS
    if (isNotSet(super->flags, CLAZZ_IS_TRUSTED) && !isClassConstant(super, super->temp.super_index)) {
      throwException(thread, clazzClassFormatError, "Superclass of %k is not a class constant (is %02x)", super, super->tags[super->temp.super_index]);

      return CLASS_LOADING_FAILED;

    }
#endif

    super = getClassConstant(super, super->temp.super_index, thread);
    if (!super) {
      throwException(thread, clazzLinkageError, "Cannot resolve superclass of %k", clazz);

      return CLASS_LOADING_FAILED;

    }

    clazz->supers[i] = super;
    woempa(1, "Class %k supers[%d] = %k\n", clazz, i, super);
    if (getClazzState(super) >= CLAZZ_STATE_SUPERS_LOADED) {
      woempa(1, "Class %k is already supersLoaded, has %d superclasses => depth of %k is %d\n", super, super->numSuperClasses, clazz, i + super->numSuperClasses + 1);
      n = i + super->numSuperClasses + 1;
      break;
    }
  }

  if (n == MAX_SUPER_CLASSES) {
      wabort(ABORT_WONKA, "Class %k has too many superclasses", clazz);
  }

  for (i= i + 1; i < n; ++i) {
    woempa(1, "Copying %k (superclass[%d] of %k) as superclass[%d] of %k\n", super->supers[i - n + super->numSuperClasses], i - n + super->numSuperClasses, super, i, clazz);
    clazz->supers[i] = super->supers[i - n + super->numSuperClasses];
  }

  woempa(1, "Class %k has total of %d superclasses\n", clazz, n);
  clazz->supers = reallocMem(clazz->supers, n * sizeof(w_clazz));
  if (!clazz->supers) {
    return CLASS_LOADING_FAILED;
  }
  clazz->numSuperClasses = n;
  super = clazz->supers[0];
#ifndef NO_HIERARCHY_CHECKS
  if (isNotSet(super->flags, CLAZZ_IS_TRUSTED)) {
    if (isSet(clazz->flags, ACC_INTERFACE) && super != clazzObject) {
      throwException(thread, clazzIncompatibleClassChangeError, "Superclass %k of %k is an interface", super, clazz);

      return CLASS_LOADING_FAILED;

    }
    if (isSet(super->flags, ACC_FINAL)) {
      woempa(9, "Violation of J+JVM Constraint 4.1.1, item 2\n");
      throwException(thread, clazzIncompatibleClassChangeError, "Superclass %k of %k is final", super, clazz);

      return CLASS_LOADING_FAILED;

    }
    if (isNotSet(super->flags, ACC_PUBLIC) && !sameRuntimePackage(clazz, super)) {
      woempa(9, "Violation of J+JVM Constraint 4.1.4\n");
      throwException(thread, clazzIncompatibleClassChangeError, "Superclass %k of %k is not accessible", super, clazz);

      return CLASS_LOADING_FAILED;

    }
  }
#endif

  for (i = n - 1; i >= 0; --i) {
    result |= mustBeSupersLoaded(clazz->supers[i]);
    if (result == CLASS_LOADING_FAILED || exceptionThrown(thread)) {

      return CLASS_LOADING_FAILED;

    }
  }

  clazz->flags |= super->flags & CLAZZ_HERITABLE_FLAGS;

  return CLASS_LOADING_SUCCEEDED;
}

#define MAX_INTERFACES 1024

static w_boolean addInterface(w_clazz new, w_clazz *if_array, w_int *l) {
  w_int i;

  for (i = 0; i < *l; ++i) {
    if (if_array[i] == new) {
      return WONKA_FALSE;
    }
  }

  if_array[(*l)++] = new;

  return WONKA_TRUE;
}

w_int loadSuperInterfaces(w_clazz clazz, w_thread thread) {
  w_int   i;
  w_int   j;
  w_int   n;
  w_clazz super;
  w_int   result = CLASS_LOADING_DID_NOTHING;

  n = 0;
  clazz->interfaces = allocMem(MAX_INTERFACES * sizeof(w_clazz));
  if (!clazz->interfaces) {
    return CLASS_LOADING_FAILED;
  }

  /*
  ** We have to do this in two passes, in order to do the Right Thing in
  ** Class/getInterfaces(). First we put the directly inherited interfaces
  ** into clazz->interfaces[], counting them in clazz->numDirectInterfaces.
  ** In this pass we also call loadSuperInterfaces() on the direct super-
  ** interfaces, and perform a number of checks; and we discard duplicates.
  */
  for (i = 0; i < clazz->temp.interface_index_count; ++i) {
#ifndef NO_FORMAT_CHECKS
    if (isNotSet(clazz->flags, CLAZZ_IS_TRUSTED) && !isClassConstant(clazz, clazz->temp.interface_index[i])) {
      throwException(thread, clazzClassFormatError, "Superinterface of %k is not a class constant (is %02x)", clazz, clazz->tags[clazz->temp.interface_index[i]]);

      return CLASS_LOADING_FAILED;

    }
#endif
    super = getClassConstant(clazz, clazz->temp.interface_index[i], thread);
    if(super == NULL){

      return CLASS_LOADING_FAILED;

    }

#ifndef NO_HIERARCHY_CHECKS
    if (super == clazz) {
      throwException(thread, clazzClassCircularityError, "Class %k is its own superinterface", clazz);

      return CLASS_LOADING_FAILED;

    }

    if (isNotSet(super->flags, ACC_PUBLIC) && !sameRuntimePackage(clazz, super)) {
      woempa(9, "Violation of J+JVM Constraint 4.1.?\n");
      throwException(thread, clazzIncompatibleClassChangeError, "Superinterface %k of %k is not accessible", super, clazz);

      return CLASS_LOADING_FAILED;

    }
#endif

    if (n == MAX_INTERFACES) {
      wabort(ABORT_WONKA, "Class %k has too many superinterfaces", clazz);
    }

    result = mustBeSupersLoaded(super);
    if (result == CLASS_LOADING_FAILED || exceptionThrown(thread)) {

      return CLASS_LOADING_FAILED;

    }

#ifndef NO_HIERARCHY_CHECKS
    if (isNotSet(clazz->flags, CLAZZ_IS_TRUSTED) && isNotSet(super->flags, ACC_INTERFACE)) {
      woempa(9, "Violation of J+JVM Constraint 4.1.?, item ? / 4.1.?, item ?\n");
      throwException(thread, clazzIncompatibleClassChangeError, "Superinterface %k of %k is not an interface", super, clazz);

      return CLASS_LOADING_FAILED;

    }
#endif

    if (addInterface(super, clazz->interfaces, &n)) {
      ++clazz->numDirectInterfaces;
      woempa(7, "Added superinterface %k to %k\n", super, clazz);
    }
    else {
      woempa(1, "Ignored duplicate superinterface %k of %k\n", super, clazz);
    }
    if (exceptionThrown(thread)) {
      break;
    }
  }

  /*
  ** In the second pass, we append all non-duplicate interfaces inherited from
  ** the direct superinterfaces and from the direct superclass.
  */
  for (i = 0; i < clazz->numDirectInterfaces; ++i) {
    super = clazz->interfaces[i];

    for (j = 0; j < super->numInterfaces; ++j) {
      if (addInterface(super->interfaces[j], clazz->interfaces, &n)) {
        woempa(7, "Added supersuperinterface %k to %k\n", super->interfaces[j], clazz);
      }
      else {
        woempa(1, "Ignored duplicate supersuperinterface %k of %k\n", super->interfaces[j], clazz);
      }
    }
    if (exceptionThrown(thread)) {
      break;
    }
  }

  super = clazz->supers[0];
  if (super) {
    for (j = 0; j < super->numInterfaces; ++j) {
      if (addInterface(super->interfaces[j], clazz->interfaces, &n)) {
        woempa(7, "Added supersuperinterface %k to %k\n", super->interfaces[j], clazz);
      }
      else {
        woempa(1, "Ignored duplicate supersuperinterface %k of %k\n", super->interfaces[j], clazz);
      }
    }
  }

  clazz->numInterfaces = n;
  woempa(7, "Class %k has total of %d superinterfaces, of which %d direct\n", clazz, n, clazz->numDirectInterfaces);
  if (clazz->temp.interface_index) {
    releaseMem(clazz->temp.interface_index);
  }
  clazz->temp.interface_index = NULL;
  clazz->interfaces = reallocMem(clazz->interfaces, n * sizeof(w_clazz));
  if (!clazz->interfaces) {
    return CLASS_LOADING_FAILED;
  }

  for (i = n - 1; i >= 0; --i) {
    result |= mustBeSupersLoaded(clazz->interfaces[i]);
    if (result == CLASS_LOADING_FAILED || exceptionThrown(thread)) {

      return CLASS_LOADING_FAILED;

    }
  }

  return CLASS_LOADING_SUCCEEDED;
}

w_int mustBeSupersLoaded(w_clazz clazz) {
  w_thread thread = currentWonkaThread;
  w_int    state = getClazzState(clazz);
  w_int    result = CLASS_LOADING_DID_NOTHING;

#ifdef RUNTIME_CHECKS
  switch (state) {
  case CLAZZ_STATE_UNLOADED:
  case CLAZZ_STATE_LOADING:
    wabort(ABORT_WONKA, "%K must be loaded before it can be linked\n", clazz);

  case CLAZZ_STATE_VERIFYING:
  case CLAZZ_STATE_VERIFIED:
    wabort(ABORT_WONKA, "Class state VERIFYING/VERIFIED doesn't exist yet!");

  default:
    ;
  }

  threadMustBeSafe(thread);
#endif

  if (state == CLAZZ_STATE_BROKEN) {
  // TODO - is the right thing to throw?
    throwException(thread, clazzNoClassDefFoundError, "%k : %w", clazz, clazz->failure_message);
  }

  if (state >= CLAZZ_STATE_SUPERS_LOADED) {

    return CLASS_LOADING_DID_NOTHING;

  }

  x_monitor_eternal(clazz->resolution_monitor);
  state = getClazzState(clazz);

  while(state == CLAZZ_STATE_SUPERS_LOADING) {
    if(clazz->resolution_thread == thread) {
      throwException(thread, clazzClassCircularityError, "Class %k is its own superclass", clazz);
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      saveFailureMessage(thread, clazz);
      x_monitor_notify_all(clazz->resolution_monitor);
      x_monitor_exit(clazz->resolution_monitor);
      
      return CLASS_LOADING_FAILED;
    }


    x_monitor_wait(clazz->resolution_monitor, CLASS_STATE_WAIT_TICKS);
    state = getClazzState(clazz);
  }

  if (state == CLAZZ_STATE_LOADED) {
    if (clazz->loader) {
      woempa(1, "Need to load supers of class %k using loader %j\n", clazz, clazz->loader);
    }
    else {
      woempa(1, "Need to load supers of class %k using bootstrap class loader\n", clazz);
    }

    if (isSet(clazz->flags, ACC_FINAL) && isSet(clazz->flags, ACC_ABSTRACT)) {
      woempa(9, "%K: Violation of J+JVM Constraint 4.1.1, item 3\n", clazz);
      throwException(currentWonkaThread, clazzIncompatibleClassChangeError, "Class %k is both FINAL and ABSTRACT", clazz);
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      saveFailureMessage(thread, clazz);
      x_monitor_notify_all(clazz->resolution_monitor);
      x_monitor_exit(clazz->resolution_monitor);

      return CLASS_LOADING_FAILED;

    }

#ifdef RUNTIME_CHECKS
    if (clazz->resolution_thread) {
      wabort(ABORT_WONKA, "clazz %k resolution_thread should be NULL\n", clazz);
    }
#endif
    clazz->resolution_thread = thread;
    setClazzState(clazz, CLAZZ_STATE_SUPERS_LOADING);
    x_monitor_exit(clazz->resolution_monitor);

    woempa(1, "Class %k super_index is %d\n", clazz, clazz->temp.super_index);
    if (clazz->temp.super_index) {
      result = loadSuperClasses(clazz, thread);
    }
    else {
      woempa(1, "Class %k super_index is 0, has no superclasses\n", clazz);
    }

    if (result != CLASS_LOADING_FAILED && clazz->temp.interface_index_count) {
      result = loadSuperInterfaces(clazz, thread);
    }

    x_monitor_eternal(clazz->resolution_monitor);
#ifdef RUNTIME_CHECKS
    if (clazz->resolution_thread != thread) {
      wabort(ABORT_WONKA, "clazz %k resolution_thread should be %p, but is %p\n", clazz, thread, clazz->resolution_thread);
    }
#endif
    clazz->resolution_thread = NULL;
    if (result == CLASS_LOADING_FAILED) {
      if (isSet(verbose_flags, VERBOSE_FLAG_INIT)) {
        w_printf("Supers %w: failed to load super class(es)\n", clazz->dotified);
      }
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      saveFailureMessage(thread, clazz);
      x_monitor_notify_all(clazz->resolution_monitor);
      x_monitor_exit(clazz->resolution_monitor);

      return result;

    }

    setClazzState(clazz, CLAZZ_STATE_SUPERS_LOADED);
    x_monitor_notify_all(clazz->resolution_monitor);
  }
  else if (state == CLAZZ_STATE_BROKEN) {
    x_monitor_exit(clazz->resolution_monitor);

    return CLASS_LOADING_FAILED;

  }

  x_monitor_exit(clazz->resolution_monitor);

  return result;
}

