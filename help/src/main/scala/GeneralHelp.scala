package com.github.philcali.argonaut;

trait GeneralHelp {
  self: ParserDefinition =>

  object Help {
    def apply(tag: Tag = Tag("h", "help", "prints this help")) = Flag(tag, {
      case _ => throw ProgramHelpException
    })
  }
}
