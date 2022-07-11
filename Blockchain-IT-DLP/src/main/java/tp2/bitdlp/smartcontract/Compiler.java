package tp2.bitdlp.smartcontract;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import tp2.bitdlp.util.result.Result;

class Compiler
{
    protected static final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    
    /**
     * Compile java source code and return a result with the path to the directory
     * where the compiled class was created.
     * 
     * @param className The class name
     * @param code The code to compile
     * @return Result with the path to the directory
     * where the compiled class was created.
     */
    public static Result<File> compile(String className, byte[] code)
    {
        Result<File> result = null;
        Path tmpDir = null;
        try
        {
            StandardJavaFileManager manager = javac.getStandardFileManager(null, null, null);

            tmpDir = Files.createTempDirectory("smart-contract");
            tmpDir.toFile().mkdirs();

            File sourceFile = new File(tmpDir.toFile(), className + ".java");
            Files.write(sourceFile.toPath(), code, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            StringWriter writer = new StringWriter();
            CompilationTask t = javac.getTask(writer, manager, null, null, null,
                manager.getJavaFileObjectsFromFiles(List.of(sourceFile)));

            if (t.call())
                result = Result.ok(tmpDir.toFile());
            else
                result = Result.error(new BadRequestException(writer.toString()));
        } catch (Exception e) {
            
            result = Result.error(new InternalServerErrorException(e.getMessage(), e));
        }

        return result;
    }
}
