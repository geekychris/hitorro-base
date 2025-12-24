/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.typesystem.valuesource;


import com.hitorro.util.core.Console;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;

public class ValueSourceFieldImplDump implements Opcodes {
	/**
	 * Code generation is avalailable by calling : org.objectweb.asm.util.ASMifier.main(new
	 * String[]{a.class.getCanonicalName()});
	 *
	 * @return
	 * @throws Exception
	 */
    /*
    ClassWriter cw = new ClassWriter(0);
FieldVisitor fv;
MethodVisitor mv;
AnnotationVisitor av0;

cw.visit(52, ACC_SUPER, "ht/util/typesystem/valuesource/a", "Lht/util/typesystem/valuesource/ValueSourceField<Ljava/lang/Object;>;", "ht/util/typesystem/valuesource/ValueSourceField", null);

{
mv = cw.visitMethod(0, "<init>", "()V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKESPECIAL, "ht/util/typesystem/valuesource/ValueSourceField", "<init>", "()V", false);
mv.visitInsn(RETURN);
mv.visitMaxs(1, 1);
mv.visitEnd();
}
     */
	public static byte[] dump(String clsName, Class targetClass, Class superClass, Method set, Method get) {
		String parent = rws(superClass.getCanonicalName());
		String className = rws(clsName);
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor cv;


		cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER,
				className,
				null,
				parent, //"ht/util/typesystem/valuesource/ValueSourceFieldImpl",
				null);

		String target = rws(targetClass.getCanonicalName());

		createConstructor(cw, parent);

		if (get != null) {
			createGetter(cw, target, get);
		}

		if (set != null) {
			if (!createSetter(cw, target, set)) {
				return null;
			}
		}
		cw.visitEnd();

		return cw.toByteArray();
	}

	/*

{
mv = cw.visitMethod(0, "<init>", "()V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKESPECIAL, "ht/util/typesystem/valuesource/ValueSourceField", "<init>", "()V", false);
mv.visitInsn(RETURN);
mv.visitMaxs(1, 1);
mv.visitEnd();
}
	 */
	private static void createConstructor(ClassWriter cw, String parent) {
		MethodVisitor cv;
		cv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		try {
			cv.visitVarInsn(ALOAD, 0);
		} catch (IncompatibleClassChangeError e) {
			Console.println("%s %e", e, e);
			return;
		}
		cv.visitMethodInsn(INVOKESPECIAL, rws(parent), "<init>", "()V", true);
		cv.visitInsn(RETURN);
		cv.visitMaxs(1, 1);
	}

	/*
	cv = cw.visitMethod(ACC_PUBLIC, "apply", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
cv.visitVarInsn(ALOAD, 1);
cv.visitTypeInsn(CHECKCAST, "ht/util/test/TestSetterGetter");
cv.visitMethodInsn(INVOKEVIRTUAL, "ht/util/test/TestSetterGetter", "get", "()Ljava/lang/Long;");
cv.visitInsn(ARETURN);
cv.visitMaxs(1, 2);
	 */
	private static void createGetter(ClassWriter cw, String target, Method get) {

		String getType = rws(get.getReturnType().getCanonicalName());

		MethodVisitor cv;
		cv = cw.visitMethod(ACC_PUBLIC, "apply", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);

		FieldType ft = FieldType.getByName(getType);
		if (ft.getFullyQualified()) {
			cv.visitVarInsn(ALOAD, 1);
			cv.visitTypeInsn(CHECKCAST, target);

			cv.visitMethodInsn(INVOKEVIRTUAL, target, get.getName(), Fmt.S("()L%s;", getType), false);
			cv.visitInsn(ARETURN);
			cv.visitMaxs(1, 2);
		} else {
			try {
				createBoxedGetter(cv, ft.getClassPath(), target, get, Fmt.S("()%s", ft.getJavaShortName()), Fmt.S("(%s)V", ft.getJavaShortName()), ft.getGetStackSize());
			} catch (IncompatibleClassChangeError e) {
				Log.util.error("Fix asm boxed getter construction");
			}
		}
	}

	private static void createBoxedGetter(MethodVisitor cv, String boxType, String target, Method get, String getPattern, String boxPattern, int stackSize) {
		cv.visitTypeInsn(NEW, boxType);
		cv.visitInsn(DUP);
		cv.visitVarInsn(ALOAD, 1);
		cv.visitTypeInsn(CHECKCAST, target);
		boolean isInterface = false;
		cv.visitMethodInsn(INVOKEVIRTUAL, target, get.getName(), getPattern, isInterface);
		cv.visitMethodInsn(INVOKESPECIAL, boxType, "<init>", boxPattern, isInterface);
		cv.visitInsn(ARETURN);
		cv.visitMaxs(stackSize, 2);
	}

    /*
    mv = cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 1);
mv.visitTypeInsn(CHECKCAST, "ht/util/typesystem/valuesource/b");
mv.visitVarInsn(ALOAD, 2);
mv.visitTypeInsn(CHECKCAST, "[I");
mv.visitTypeInsn(CHECKCAST, "[I");
mv.visitMethodInsn(INVOKEVIRTUAL, "ht/util/typesystem/valuesource/b", "setEdges", "([I)V", false);
mv.visitInsn(RETURN);
mv.visitMaxs(2, 3);
mv.visitEnd();
     */

	/**
	 * @param cw
	 * @param target
	 * @param set
	 */
	private static boolean createSetter(ClassWriter cw, String target, Method set) {
		Class[] cls = set.getParameterTypes();
		String setType = rws(cls[0].getCanonicalName());
		String bracket;
		boolean bracketed;
		if (setType.contains("[")) {
			bracket = "[";
			setType = setType.substring(0, setType.length() - 2);
			bracketed = true;

			// for now lets ignore this case and fall back on reflections than byte coded (Chris cant figure it out)
			return false;
		} else {
			bracket = "";
			bracketed = false;
		}
		FieldType ft = FieldType.getByName(setType);

		MethodVisitor cv;

		if (false) {
			// trying to be smart about the object passing
			getCVComplexArgs(cw, setType, bracket, bracketed, ft);
		} else {
			// using object object passing to the setter.
			cv = cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
		}

		cv.visitCode();
		cv.visitVarInsn(ALOAD, 1);
		cv.visitTypeInsn(CHECKCAST, target);
        /*if (ft == FieldType.booleanType)
        {
            Console.println();
        } */

		// if (bracketed)
		{
			cv.visitVarInsn(ALOAD, 2);
		}
        /*else
        {
            cv.visitVarInsn(ft.getOpcodeLoad(), 2);
        } */


		if (ft.getFullyQualified()) {
			if (bracketed) {
				/**
				 * This fix took me forever to find.  The non array form does not have the L[..;
				 * Ultimately I had todo some BASIC debugging by using asmfieldgenerator to generate two
				 * object valuesource wrappers one with and one without [].  Then diffing them.
				 */
				cv.visitTypeInsn(CHECKCAST, Fmt.S("[L%s;", setType));
			} else {
				cv.visitTypeInsn(CHECKCAST, setType);
			}
			cv.visitMethodInsn(INVOKEVIRTUAL, target, set.getName(), Fmt.S("(%sL%s;)V", bracket, setType), false);
		} else {
			cv.visitTypeInsn(CHECKCAST, ft.getClassPath());
			//String s = Fmt.S("(%sL%s)V", bracket, ft.getJavaShortName());
			String s = Fmt.S("(%s%s)V", bracket, ft.getJavaShortName());
			cv.visitMethodInsn(INVOKEVIRTUAL, ft.getClassPath(), ft.getValueString(), Fmt.S("()%s", ft.getJavaShortName()));
			cv.visitMethodInsn(INVOKEVIRTUAL, target, set.getName(), s);
		}
		cv.visitInsn(RETURN);
		cv.visitMaxs(ft.getVisitParam(), ft.getVisitBParam());
		return true;
	}

	private static void getCVComplexArgs(final ClassWriter cw, final String setType, final String bracket, final boolean bracketed, final FieldType ft) {
		MethodVisitor cv;
		if (ft.getFullyQualified()) {
			cv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "setValue", Fmt.S("(Ljava/lang/Object;%sL%s;)V", bracket, setType), null, null);
		} else {
			if (bracketed) {

				String s = Fmt.S("(Ljava/lang/Object;%s%s)V", bracket, ft.getJavaShortName());
				cv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "setValue", s, null, null);
			} else {
				String s = Fmt.S("(Ljava/lang/Object;%s)V", ft.getJavaShortName());
				cv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "setValue", s, null, null);
			}
		}
	}

	private static final String rws(String s) {
		return StringUtil.replace(s, ".", "/");
	}
}

/*
{

integer array
=============

mv = cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;[I)V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 1);
mv.visitTypeInsn(CHECKCAST, "ht/util/typesystem/valuesource/b");
mv.visitVarInsn(ALOAD, 2);
mv.visitMethodInsn(INVOKEVIRTUAL, "ht/util/typesystem/valuesource/b", "setEdges", "([I)V");
mv.visitInsn(RETURN);
mv.visitMaxs(2, 3);
mv.visitEnd();
}

{
integer
=======

mv = cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;I)V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 1);
mv.visitTypeInsn(CHECKCAST, "ht/util/typesystem/valuesource/b");
mv.visitVarInsn(ILOAD, 2);
mv.visitMethodInsn(INVOKEVIRTUAL, "ht/util/typesystem/valuesource/b", "setEdges", "(I)V");
mv.visitInsn(RETURN);
mv.visitMaxs(2, 3);
mv.visitEnd();


boolean
========

mv = cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;Z)V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 1);
mv.visitTypeInsn(CHECKCAST, "ht/util/typesystem/valuesource/boop");
mv.visitVarInsn(ILOAD, 2);
mv.visitMethodInsn(INVOKEVIRTUAL, "ht/util/typesystem/valuesource/boop", "setEdges", "(Z)V");
mv.visitInsn(RETURN);
mv.visitMaxs(2, 3);
mv.visitEnd();


string
======
mv = cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;Ljava/lang/String;)V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 1);
mv.visitTypeInsn(CHECKCAST, "ht/util/typesystem/valuesource/b");
mv.visitVarInsn(ALOAD, 2);
mv.visitMethodInsn(INVOKEVIRTUAL, "ht/util/typesystem/valuesource/b", "setEdges", "(Ljava/lang/String;)V");
mv.visitInsn(RETURN);
mv.visitMaxs(2, 3);
mv.visitEnd();

long
====

mv = cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;J)V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 1);
mv.visitTypeInsn(CHECKCAST, "ht/util/typesystem/valuesource/b");
mv.visitVarInsn(LLOAD, 2);
mv.visitMethodInsn(INVOKEVIRTUAL, "ht/util/typesystem/valuesource/b", "setEdges", "(J)V");
mv.visitInsn(RETURN);
mv.visitMaxs(3, 4);
mv.visitEnd();

}
 */

/**
 * This is the original code:
 * <p>
 * private static void createSetter (ClassWriter cw, String target, Method set)
 * {
 * CodeVisitor cv;
 * cv = cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
 * cv.visitVarInsn(ALOAD, 1);
 * cv.visitTypeInsn(CHECKCAST, target);
 * cv.visitVarInsn(ALOAD, 2);
 * <p>
 * Class cls[] = set.getParameterTypes();
 * String setType = rws(cls[0].getCanonicalName());
 * String pattern;
 * int visitParamA = 2;
 * if (setType.equals("int"))
 * {
 * // integer case
 * cv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
 * cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
 * pattern = "(I)V";
 * }
 * else if (setType.equals("boolean"))
 * {
 * cv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
 * cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
 * pattern = "(Z)V";
 * }
 * else if (setType.equals("double"))
 * {
 * cv.visitTypeInsn(CHECKCAST, "java/lang/Double");
 * cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
 * pattern = "(D)V";
 * visitParamA = 3;
 * }
 * else if (setType.equals("long"))
 * {
 * cv.visitTypeInsn(CHECKCAST, "java/lang/Long");
 * cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
 * pattern = "(J)V";
 * visitParamA = 3;
 * }
 * else
 * {
 * // normal case
 * cv.visitTypeInsn(CHECKCAST, setType);
 * pattern = Fmt.S("(L%s;)V", setType);
 * }
 * cv.visitMethodInsn(INVOKEVIRTUAL, target, set.getName(), pattern);
 * cv.visitInsn(RETURN);
 * cv.visitMaxs(visitParamA, 3);
 * }
 */