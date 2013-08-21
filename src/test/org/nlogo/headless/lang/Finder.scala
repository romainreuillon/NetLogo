// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import java.io.File
import org.nlogo.api.FileIO.file2String
import org.scalatest.{ FunSuite, Tag }
import org.nlogo.api
import org.nlogo.util.SlowTest

/// top level entry points

class TestCommands extends Finder {
  override def files =
    TxtsInDir("test/commands")
}
class TestReporters extends Finder {
  override def files =
    TxtsInDir("test/reporters")
}
class TestModels extends Finder {
  override def files =
    TxtsInDir("models/test")
      .filterNot(_.getName.startsWith("checksums"))
}

/// common infrastructure

// don't use FixtureSuite here because we may need two fixtures, not just
// one, and FixtureSuite assumes one - ST 8/7/13

trait Finder extends FunSuite with SlowTest {
  def files: Iterable[File]
  // parse tests first, then run them
  for(t <- parseFiles(files) if shouldRun(t))
    test(t.fullName, new Tag(t.suiteName){}, new Tag(t.fullName){}) {
      for (mode <- t.modes)
        Fixture.withFixture(s"${t.fullName} ($mode)"){
          fixture =>
            val nonDecls = t.entries.filterNot(_.isInstanceOf[Declaration])
            fixture.declare(HeadlessWorkspace.TestDeclarations +
              t.entries.collect{
                case d: Declaration => d.source}.mkString("\n"))
            nonDecls.foreach{
              case Open(path) =>
                fixture.workspace.open(path)
              case Declaration(content) =>
                fixture.declare(content)
              case command: Command =>
                fixture.runCommand(command, mode)
              case reporter: Reporter =>
                fixture.runReporter(reporter, mode)
            }
        }
    }
  def parseFiles(files: Iterable[File]): Iterable[LanguageTest] =
    for {
      f <- files if !f.isDirectory
      test <- parseFile(f)
    } yield test

  def parseFile(f: File): List[LanguageTest] = {
    def preprocessStackTraces(s: String) =
      s.replace("\\\n  ", "\\n")
    val suiteName =
      if(f.getName == "tests.txt")
        f.getParentFile.getName
      else
        f.getName.replace(".txt", "")
    Parser.parse(suiteName, preprocessStackTraces(file2String(f.getAbsolutePath)))
  }
  // on the core branch the _3D tests are gone, but extensions tests still have them since we
  // didn't branch the extensions, so we still need to filter those out - ST 1/13/12
  def shouldRun(t: LanguageTest) =
    !t.testName.endsWith("_3D") &&
    !t.testName.startsWith("Generator")
}

case class TxtsInDir(dir: String) extends Iterable[File] {
  override def iterator =
    new File(dir).listFiles
      .filter(_.getName.endsWith(".txt"))
      .filterNot(_.getName.containsSlice("SDM"))
      .filterNot(_.getName.containsSlice("HubNet"))
      .iterator
}