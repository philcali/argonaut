package com.github.philcali.argonaut

import annotation.StaticAnnotation

case class Meta(name: String, version: String, description: String = "", author: String = "") extends StaticAnnotation
case class Tag(short: String = "", long: String = "", description: String = "") extends StaticAnnotation
class Required extends StaticAnnotation
