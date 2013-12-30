package com.github.philcali.argonaut;
package test;

import org.scalatest.{ FlatSpec, Matchers }

class ParserDefinitionSpec extends FlatSpec with Matchers {
  case class TestParams(
    name: String = "default",
    volume: Int = 7,
    wetRun: Boolean = false,
    members: Seq[String] = Nil
  )

  object TestParamsParser extends ParserDefinition {
    type Mapper = TestParams

    val runner = SingleArg(Tag("n", "name"), {
      case (test, Some(n)) => test.copy(name = n)
      case (test, None) => throw new RuntimeException("Provide name")
    })

    val volume = SingleArg(Tag("v", "vol"), {
      case (test, Some(vol)) if vol.toInt <= 10 => test.copy(volume = vol.toInt)
      case _ => throw new RuntimeException("Enter a valid number")
    })

    val wetRun = Flag(Tag(long = "run"), {
      case (test, run) => test.copy(wetRun = run)
    })

    val members = MultiArg(Tag("m", "members"), {
      case (test, mem) => test.copy(members = mem)
    })

    val default = TestParams()
    val meta = Meta("Test Program", "[BETA]", author = "Philip Cali")
    val program = runner :: volume :: wetRun :: members :: Nil
  }

  "A ParserDefinition" should "parse input" in {
    val params = TestParamsParser.parse("-n Philip -v 9 --run".split(" "))

    params.name should be === "Philip"
    params.volume should be === 9
    params.wetRun should be (true)
  }

  it should "validate input" in {
    intercept[RuntimeException] {
      TestParamsParser.parse("-n -v 10".split(" "))
    }

    intercept[RuntimeException] {
      TestParamsParser.parse("-v 12".split(" "))
    }
  }

  it should "be formattable" in {
    TestParamsParser format HelpFormatter
  }
}
