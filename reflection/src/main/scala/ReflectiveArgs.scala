package com.github.philcali.argonaut;

import reflect.runtime.universe;

abstract class ReflectiveArgs[T: universe.TypeTag] extends ParserDefinition {
  type Mapper = T

  val instanceMirror: universe.InstanceMirror
  val tagOverrides: Map[String, Tag => Tag]
  val callbackOverrides: Map[String, String => Any] = Map[String, String => Any]()

  val mirror = universe.runtimeMirror(getClass.getClassLoader)
  val theType = universe.typeOf[Mapper]

  def createTag(tag: Tag) = tagOverrides.get(tag.long).map(_.apply(tag)).getOrElse(tag)

  def reflectiveMeta[M: universe.TypeTag] = {
    import universe.{ Literal, Constant }
    val selfType = universe.typeOf[M]
    val metaType = universe.typeOf[Meta]
    selfType.typeSymbol.asClass.annotations.find(_.tpe =:= metaType) match {
      case Some(annotation) => annotation.scalaArgs match {
        case List(
          Literal(Constant(name)),
          Literal(Constant(version)),
          Literal(Constant(description)),
          Literal(Constant(author))) =>
        Meta(name.toString, version.toString, description.toString, author.toString)
        case _ => throw ReflectiveException("Failed to pull meta information.")
      }
      case None => throw ReflectiveException("ParserDefinition was not annotated with @Meta.")
    }
  }

  def reflectiveArgs = {
    val values = theType.declarations
      .filter(_.toString.startsWith("value"))
      .filter(!_.isMethod)
      .map(_.asTerm)

    def setDefault(block: => Unit) = { block; default }
    values.map {
      value =>
      val field = instanceMirror.reflectField(value.asTerm)
      val name = value.name.toString.trim
      val tag = createTag(Tag(name.substring(0, 1), name))

      value.typeSignature match {
        case x if x =:= universe.definitions.BooleanTpe =>
        Flag(tag, { case (_, b) => setDefault(field.set(true)) })
        case x if x <:< universe.typeOf[Seq[_]] =>
        MultiArg(tag, { case (_, args) => setDefault(field.set(args)) })
        case x =>
        SingleArg(tag, {
          case (_, Some(arg)) => setDefault(
            field.set(
              if (x =:= universe.definitions.IntTpe) arg.toInt
              else if (x =:= universe.definitions.DoubleTpe) arg.toDouble
              else if (x =:= universe.definitions.LongTpe) arg.toLong
              else callbackOverrides
                .get(name)
                .map(_.apply(arg))
                .getOrElse(arg)
            )
          )
          case (x, None) => x
        })
      }
    }.toList
  }
}
