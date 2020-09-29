package com.pa.paperless.video.opengles;

import android.content.Context;
import android.opengl.GLES20;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author ywl
 * @date 2017-12-16
 */
public class WlShaderUtils {

    /**
     * 将glsl文件内容转换为字符串
     * @param context
     * @param resId
     * @return
     */
    public static String readRawTextFile(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param shaderType 类型 Vertex或Fragment
     * @param source 源码字符串
     * @return
     */
    public static int loadShader(int shaderType, String source) {
        //创建一个vertex shader程序，返回的是它的句柄
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            //告诉OpenGL，这一坨字符串里面是vertex shader的源码
            GLES20.glShaderSource(shader, source);
            //编译vertex shader
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            //验证shader program是否错误，获取验证状态
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {//验证结果为0 则表示正常
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }
        //创建shader program句柄
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            //把vertex shader添加到program
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            //把fragment shader添加到program
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            //做链接，可以理解为把两种shader进行融合，做好投入使用的最后准备工作
            GLES20.glLinkProgram(program);
            //验证
            int[] linkStatus = new int[1];
            //获取验证的状态
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                //删除该程序
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public static void checkGlError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(label + ": glError " + error);
        }
    }

}
