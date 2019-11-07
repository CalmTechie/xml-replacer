package ru.korolev

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import java.io.*
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashSet

private val TAGS = Properties()
private val IGNORE_SET = HashSet<String>()
private val NEW_TAGS_SET = HashSet<String>()

private var rewriteFiles = false

private var handledCount = 0
private var unHandledCount = 0
private var tagsChangedCount = 0

/**
 * Entry function, covering all xml processing steps
 *
 * The only purpose of this class - replace text content of tags presented in [tagsFilePath] dictionary in every file
 * in [xmlFolderPath] folder. As a result function will generate new files (or [rewrite] them) with text content replaced.
 * Also, function will generate [newTagsFilePath] file with unhandled tags not presented in [ignoreFilePath] file.
 * The text of tags with child tags will not be replaced.
 *
 * @param xmlFolderPath folder with .xml files
 * @param tagsFilePath file with dictionary (format is TAG:TEXT)
 * @param rewrite if 'false', new files will have same name with 'm' letter first
 * @param ignoreFilePath file with list of ignore tags separated with '\r\n' (Windows format)
 * @param newTagsFilePath file, where all unhandled tags will be written ('new-tags.txt' by default)
 */
fun process(
    xmlFolderPath: String,
    tagsFilePath: String,
    rewrite: Boolean = false,
    ignoreFilePath: String? = null,
    newTagsFilePath: String? = null
) {
    rewriteFiles = rewrite
    loadFiles(tagsFilePath, ignoreFilePath)
    processFolder(xmlFolderPath)
    saveNewTagsFile(newTagsFilePath ?: "new-tags.txt")
    printSummaryToConsole()
}

private fun loadFiles(tagsFilePath: String, ignoreFilePath: String?) {
    TAGS.load(InputStreamReader(FileInputStream(tagsFilePath), UTF_8))
    ignoreFilePath?.let { IGNORE_SET.addAll(String(Files.readAllBytes(Paths.get(it)), UTF_8).split("\r\n")) }
}

private fun processFolder(xmlFolderPath: String) {
    Files.walk(Paths.get(xmlFolderPath)).use {
        it.filter { path -> Files.isRegularFile(path) }.forEach(::handleFile)
    }
}

private fun handleFile(path: Path) {
    val fileName = path.fileName.toString()
    val doc: Document
    try {
        doc = SAXBuilder().build(InputStreamReader(ByteArrayInputStream(Files.readAllBytes(path)), UTF_8))
    } catch (e: Exception) {
        unHandledCount++
        System.err.println("ALARM! $fileName not handled!")
        return
    }
    handleElement(doc.rootElement)
    saveHandledFile(path, doc)
}

private fun handleElement(el: Element) {
    val name = el.name.trim().toLowerCase()
    if (el.children.isEmpty()) {
        if (TAGS.containsKey(name)) {
            el.text = TAGS.getProperty(name)
            tagsChangedCount++
        } else if (!IGNORE_SET.contains(name)) {
            NEW_TAGS_SET.add(name)
        }
    } else {
        el.children.forEach(::handleElement)
    }
}

private fun saveHandledFile(path: Path, doc: Document) {
    val absolutePath = path.toAbsolutePath().toString()
    val folder = absolutePath.substring(0, absolutePath.lastIndexOf(File.separatorChar))
    val file = "${if (!rewriteFiles) "m" else ""}${path.fileName}.xml"
    val filePath = "$folder\\$file"
    try {
        FileOutputStream(filePath).use { XMLOutputter(Format.getPrettyFormat()).output(doc, it) }
    } catch (e: IOException) {
        System.err.println("ALARM! Fail saving handled file")
        unHandledCount++
    }
    println("${path.fileName} saved")
    handledCount++
}

private fun saveNewTagsFile(newTagsFilePath: String) {
    if (NEW_TAGS_SET.isEmpty())
        return
    val sb = StringBuilder()
    NEW_TAGS_SET.forEach { sb.append(it).append("\r\n") }
    try {
        FileOutputStream(newTagsFilePath).use { it.write(sb.toString().toByteArray()) }
    } catch (e: IOException) {
        System.err.println("ALARM! Fail saving new-tags file")
    }
}

private fun printSummaryToConsole() {
    println(
        """Finished!
        |Files handled: $handledCount
        |Tags changed: $tagsChangedCount
        |Files not handled: $unHandledCount
        |New tags found: ${NEW_TAGS_SET.size}
    """.trimMargin()
    )
}