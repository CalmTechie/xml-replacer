package ru.korolev

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

private val CMD_OPTIONS = Options()

private var xmlFolderPath = ""
private var tagsFilePath = ""
private var ignoreFilePath: String? = ""
private var newTagsFilePath: String? = ""
private var rewriteFiles: Boolean = false

/**
 * Entry point, using replacer via cmd arguments
 */
fun main(args: Array<String>) {
    initializeOptions()
    parseCommandLineArguments(args)
    process(xmlFolderPath, tagsFilePath, rewriteFiles, ignoreFilePath, newTagsFilePath)
}

/**
 * Handy option-initialize function
 */
private fun initializeOptions() {
    CMD_OPTIONS
        .addOption(Option.builder("x").hasArg().valueSeparator().required().build())
        .addOption(Option.builder("t").hasArg().valueSeparator().required().build())
        .addOption(Option.builder("i").hasArg().valueSeparator().build())
        .addOption(Option.builder("n").hasArg().valueSeparator().build())
        .addOption(Option.builder("r").build())
}

/**
 * Function, that parses cmd [args]
 */
private fun parseCommandLineArguments(args: Array<String>) {
    val cmd = DefaultParser().parse(CMD_OPTIONS, args)
    xmlFolderPath = cmd.getOptionValue('x')
    tagsFilePath = cmd.getOptionValue('t')
    ignoreFilePath = cmd.getOptionValue('i')
    newTagsFilePath = cmd.getOptionValue('n')
    rewriteFiles = cmd.hasOption('r')
}