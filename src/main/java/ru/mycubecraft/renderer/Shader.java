package ru.mycubecraft.renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import ru.mycubecraft.engine.Material;
import ru.mycubecraft.engine.graph.DirectionalLight;
import ru.mycubecraft.engine.graph.PointLight;
import ru.mycubecraft.engine.graph.SpotLight;
import ru.mycubecraft.engine.graph.weather.Fog;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private final String filepath;
    private final FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
    private final Map<String, Integer> varLocationsMap;
    private int shaderProgramID;
    private boolean beingUsed = false;
    private String vertexSource;
    private String fragmentSource;

    public Shader(String filepath) {
        this.filepath = filepath;
        this.varLocationsMap = new HashMap<>();
        try {
            String source = new String(Files.readAllBytes(Paths.get(filepath)));
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

            // Find the first pattern after #type 'pattern'
            int index = source.indexOf("#type") + 6;
            int eol = source.indexOf("\n", index);
            String firstPattern = source.substring(index, eol).trim();

            // Find the second pattern after #type 'pattern'
            index = source.indexOf("#type", eol) + 6;
            eol = source.indexOf("\n", index);
            String secondPattern = source.substring(index, eol).trim();

            if (firstPattern.equals("vertex")) {
                vertexSource = splitString[1];
            } else if (firstPattern.equals("fragment")) {
                fragmentSource = splitString[1];
            } else {
                throw new IOException("Unexpected token '" + firstPattern + "'");
            }

            if (secondPattern.equals("vertex")) {
                vertexSource = splitString[2];
            } else if (secondPattern.equals("fragment")) {
                fragmentSource = splitString[2];
            } else {
                throw new IOException("Unexpected token '" + secondPattern + "'");
            }
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error: Could not open file for shader: '" + filepath + "'";
        }
    }

    public void compile() {
        // ============================================================
        // Compile and link shaders
        // ============================================================
        int vertexID, fragmentID;

        // First load and compile the vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);//0x8B31
        // Pass the shader source to the GPU
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        // Check for errors in compilation
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);

        //GL_TRUE  = 1 GL_FALSE = 0;
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.err.println("ERROR: '" + filepath + "'\n\tVertex shader compilation failed.");
            System.err.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        // First load and compile the vertex shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);//0x8B30
        // Pass the shader source to the GPU
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        // Check for errors in compilation
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.err.println("ERROR: '" + filepath + "'\n\tFragment shader compilation failed.");
            System.err.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        // Link shaders and check for errors
        shaderProgramID = glCreateProgram();//Creates a program object.
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        // Check for linking errors
        success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.err.println("ERROR: '" + filepath + "'\n\tLinking of shaders failed.");
            System.err.println(glGetProgramInfoLog(shaderProgramID, len));
            assert false : "";
        }
    }

    public void use() {
        if (!beingUsed) {
            // Bind shader program
            glUseProgram(shaderProgramID);
            beingUsed = true;
        }
    }

    public void detach() {
        glUseProgram(0);
        beingUsed = false;
    }

    public void uploadMat4f(String varName, Matrix4f mat4) {
        int varLocation = getVarLocation(varName);
        use();
        mat4.get(matBuffer);
        GL20.glUniformMatrix4fv(varLocation, false, matBuffer);
    }

    public void uploadMat3f(String varName, Matrix3f mat3) {
        int varLocation = getVarLocation(varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
        mat3.get(matBuffer);
        GL20.glUniformMatrix3fv(varLocation, false, matBuffer);
    }

    public void uploadVec4f(String varName, Vector4f vec) {
        int varLocation = getVarLocation(varName);
        use();
        //glUniform ??? Specify the value of a uniform variable for the current program object
        glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
    }

    public void uploadVec3f(String varName, Vector3f vec) {
        int varLocation = getVarLocation(varName);
        use();
        //glUniform ??? Specify the value of a uniform variable for the current program object
        glUniform3f(varLocation, vec.x, vec.y, vec.z);
    }

    public void uploadVec2f(String varName, Vector2f vec) {
        int varLocation = getVarLocation(varName);
        use();
        //glUniform ??? Specify the value of a uniform variable for the current program object
        glUniform2f(varLocation, vec.x, vec.y);
    }

    public void uploadFloat(String varName, float val) {
        int varLocation = getVarLocation(varName);
        use();
        //glUniform ??? Specify the value of a uniform variable for the current program object
        glUniform1f(varLocation, val);
    }

    public void uploadInt(String varName, int val) {
        int varLocation = getVarLocation(varName);
        use();
        //glUniform ??? Specify the value of a uniform variable for the current program object
        glUniform1i(varLocation, val);
    }

    public void uploadTexture(String varName, int slot) {
        int varLocation = getVarLocation(varName);
        use();
        //glUniform ??? Specify the value of a uniform variable for the current program object
        glUniform1i(varLocation, slot);
    }

    public void uploadIntArray(String varName, int[] array) {
        int varLocation = getVarLocation(varName);
        use();
        //glUniform ??? Specify the value of a uniform variable for the current program object
        glUniform1iv(varLocation, array);
    }

    public void setUniform(String varName, Vector3f value) {
        int varLocation = getVarLocation(varName);
        use();
        glUniform3f(varLocation, value.x, value.y, value.z);
    }

    public void setUniform(String varName, float x, float y) {
        int varLocation = getVarLocation(varName);
        use();
        glUniform2f(varLocation, x, y);
    }

    public void setUniform(String uniformName, DirectionalLight dirLight) {
        setUniform(uniformName + ".colour", dirLight.getColor());
        setUniform(uniformName + ".direction", dirLight.getDirection());
        uploadFloat(uniformName + ".intensity", dirLight.getIntensity());
    }

    public void setUniform(String uniformName, Material material) {
        uploadVec4f(uniformName + ".ambient", material.getAmbientColour());
        uploadVec4f(uniformName + ".diffuse", material.getDiffuseColour());
        uploadVec4f(uniformName + ".specular", material.getSpecularColour());
        uploadInt(uniformName + ".hasTexture", material.isTextured() ? 1 : 0);
        uploadFloat(uniformName + ".reflectance", material.getReflectance());
    }

    public void setUniform(String uniformName, Fog fog) {
        uploadInt(uniformName + ".activeFog", fog.isActive() ? 1 : 0);
        uploadVec3f(uniformName + ".colour", fog.getColour());
        uploadFloat(uniformName + ".density", fog.getDensity());
    }

    public void setUniform(String uniformName, PointLight pointLight, int pos) {
        setUniform(uniformName + "[" + pos + "]", pointLight);
    }

    public void setUniform(String uniformName, SpotLight spotLight, int pos) {
        setUniform(uniformName + "[" + pos + "]", spotLight);
    }

    public void setUniform(String uniformName, SpotLight spotLight) {
        setUniform(uniformName + ".pl", spotLight.getPointLight());
        setUniform(uniformName + ".conedir", spotLight.getConeDirection());
        uploadFloat(uniformName + ".cutoff", spotLight.getCutOff());
    }

    public void setUniform(String uniformName, PointLight pointLight) {
        setUniform(uniformName + ".colour", pointLight.getColor());
        setUniform(uniformName + ".position", pointLight.getPosition());
        uploadFloat(uniformName + ".intensity", pointLight.getIntensity());
        PointLight.Attenuation att = pointLight.getAttenuation();
        uploadFloat(uniformName + ".att.constant", att.getConstant());
        uploadFloat(uniformName + ".att.linear", att.getLinear());
        uploadFloat(uniformName + ".att.exponent", att.getExponent());
    }

    private int getVarLocation(String varName) {
        int varLocation;
        if (!varLocationsMap.containsKey(varName)) {
            varLocation = glGetUniformLocation(shaderProgramID, varName);
            varLocationsMap.put(varName, varLocation);
        } else {
            varLocation = varLocationsMap.get(varName);
        }
        return varLocation;
    }
}
