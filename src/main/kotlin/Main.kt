import com.facebook.soloader.*;
import java.io.File
import java.util.*

val soRoot = "/mnt/d/RNApp68/android/app/build/react-ndk/exported/x86_64/";

fun enumDeps(soName: String) {
    val soPath = soRoot + soName;
    val soFile = File(soPath);
    if(!soFile.exists())
        return;

    val elf = ElfFileChannel(soFile);
    val deps = NativeDeps.getDependencies(soName, elf)
    println(soName + " : " + Arrays.toString(deps))

    deps.forEach { enumDeps(it)  }
}

fun main(args: Array<String>) {

    enumDeps("libreactnativejni.so");

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
}