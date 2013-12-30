package com.github.philcali.argonaut;

import util.Try

trait ParserDefinition {
  type Mapper

  trait ParsedArg[X] {
    val tag: Tag
    val callback: (Mapper, X) => Mapper

    def matches(arg: String) = ("-" + tag.short == arg || "--" + tag.long == arg)
    def apply(index: Int, args: Seq[String], pass: Mapper): Mapper
  }

  case class SingleArg(tag: Tag, callback: (Mapper, Option[String]) => Mapper) extends ParsedArg[Option[String]] {
    def apply(index: Int, args: Seq[String], pass: Mapper) = {
      Try(args(index + 1))
        .filter(!isProgramArgument(_))
        .map(e => callback(pass, Some(e)))
        .recover({ case _ => callback(pass, None) })
        .get
    }
  }

  case class Flag(tag: Tag, callback: (Mapper, Boolean) => Mapper) extends ParsedArg[Boolean] {
    def apply(index: Int, args: Seq[String], pass: Mapper) = callback(pass, true)
  }

  case class MultiArg(tag: Tag, callback: (Mapper, Seq[String]) => Mapper) extends ParsedArg[Seq[String]] {
    def apply(index: Int, args: Seq[String], pass: Mapper) =  {
      Try(args.slice(index + 1, args.length).takeWhile(!isProgramArgument(_)))
        .map(callback(pass, _))
        .recover({ case _ => callback(pass, Nil) })
        .get
      }
  }

  val default: Mapper
  val meta: Meta
  val program: Seq[ParsedArg[_]]

  def parse(args: Seq[String]) = {
    (default /: args.zipWithIndex)({
      case (ret, (arg, index)) =>
        program.find(_.matches(arg)) match {
          case Some(parser) => parser.apply(index, args, ret)
          case None => ret
        }
    })
  }

  def isProgramArgument(arg: String) = program.find(_.matches(arg)).isDefined

  def format(formatter: Formatter) = formatter format this
}
