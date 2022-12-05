import com.facebook.soloader.*;
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

//val soRoot = "/mnt/d/RNApp68/android/app/build/react-ndk/exported/x86_64/";
val soRoot = "C:\\rn_libs\\x86_64\\"
val blackList = listOf("libc++_shared.so", "libandroid.so", "libc.so", "libdl.so", "libm.so", "liblog.so")

fun enumDeps(processedSos: LinkedHashMap<String, ArrayList<String>>, soName: String) {
    if(soName in blackList || soName in processedSos.keys)
        return;

    val soPath = soRoot + soName;
    val soFile = File(soPath);
    if(!soFile.exists())
        return;

    val elf = ElfFileChannel(soFile);
    val deps = NativeDeps.getDependencies(soName, elf)

    val filteredSos = deps.asList().minus(blackList)
    processedSos[soName] = ArrayList<String>(filteredSos);

    deps.forEach { enumDeps(processedSos, it)  }
}

fun printMap(processedSos: LinkedHashMap<String, java.util.ArrayList<String>>) {
    for(key in processedSos.keys) {
        println(key + " :: " + processedSos[key])
    }
}


fun printMap2(processedSos: LinkedHashMap<Int, java.util.ArrayList<Int>>) {
    for(key in processedSos.keys) {
        println("${key} :: ${processedSos[key]}")
    }
}

fun listEquals(list1: List<String>, list2: MutableSet<String>): Boolean {
    return (list1.size == list2.size && list1.containsAll(list2) && list2.containsAll(list1))
}

fun printProcessedMap(processedSos: LinkedHashMap<String, ArrayList<String>>,
                      soListInLoadOrder: ArrayList<String>,
                      processedSosIndexed : LinkedHashMap<Int, ArrayList<Int>>,
                      processedSosIndexed2 : ArrayList<ArrayList<Int>>) {
    var loadedSos = ArrayList<String>();

    val allSos = processedSos.keys
    while(true) {
        if(listEquals(loadedSos, allSos))
            break;

        for (key in processedSos.keys.minus(loadedSos)) {
            val depSos = processedSos[key];
            if (loadedSos.containsAll(depSos!!)) {
                loadedSos.add(key)

                // println(key + " ::: " + processedSos[key])

                soListInLoadOrder.add(key);
                var depsIndexed = processedSos[key]?.map { soListInLoadOrder.indexOf(it) } as ArrayList<Int>
                processedSosIndexed[soListInLoadOrder.indexOf(key)] = depsIndexed
                processedSosIndexed2.add(depsIndexed)
            }
        }
    }
}

fun main(args: Array<String>) {
    val processedSos = LinkedHashMap<String, ArrayList<String>>();
    val soListInLoadOrder = ArrayList<String>();
    val processedSosIndexed = LinkedHashMap<Int, ArrayList<Int>>();
    val processedSosIndexed2 = ArrayList<ArrayList<Int>>();

    // enumDeps(processedSos, "libreactnativejni.so");
    enumDeps(processedSos, "libfabricjni.so");
    // printMap(processedSos);
    printProcessedMap(processedSos, soListInLoadOrder, processedSosIndexed, processedSosIndexed2);

    var sosStr = ("[" + soListInLoadOrder.map { "\"" + it + "\"" }.joinToString(",") + "]")
    var depsStr = "{" + processedSosIndexed2.joinToString(",").replace('[', '{').replace(']', '}') + "}";

    println(sosStr)
    println(depsStr)
}
