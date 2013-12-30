package com.github.philcali.argonaut;

import java.text.SimpleDateFormat
import java.util.Date

object HelpFormatter extends Formatter {
  def format(parser: ParserDefinition) = formatMeta(parser.meta) + formatArgs(parser)

  def formatMeta(meta: Meta) = {
    val year = (new SimpleDateFormat("yyyy")).format(new Date)
    s"""
    | ${meta.name} - ${meta.description}
    | Copyright ${year}, ${meta.version} ${meta.author}
    |
    |""".stripMargin
  }

  def formatArgs(commandline: ParserDefinition) = {
    commandline.program.map {
      parser =>
      val short = if (parser.tag.short.isEmpty) "" else s"-${parser.tag.short}"
      val long = if (parser.tag.short.isEmpty) "" else s"--${parser.tag.long}"
      val argEntry = s"  ${short} ${long}"
      val fullText = parser match {
        case commandline.SingleArg(tag, _) =>
        s"${argEntry} <${tag.long}>"
        case commandline.Flag(tag, _) =>
        s"${argEntry}"
        case commandline.MultiArg(tag, _) =>
        s"${argEntry} [${tag.long}1, ${tag.long}2, ...]"
      }
      parser.tag.description.isEmpty match {
        case true => fullText
        case false => s"${fullText}\n     ${parser.tag.description}"
      }
    } mkString "\n\n"
  }
}

