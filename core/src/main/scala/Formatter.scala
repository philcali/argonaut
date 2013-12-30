package com.github.philcali.argonaut;

trait Formatter {
  def format(parser: ParserDefinition): String
}
