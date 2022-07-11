import com.intellij.testFramework.ParsingTestCase
import org.aya.intellij.AyaFileType
import org.aya.intellij.parser.AyaParserDefinition

abstract class AyaParsingTestCase(dataPath: String) : ParsingTestCase(dataPath, AyaFileType.defaultExtension, AyaParserDefinition()) {
  override fun getTestDataPath() = "src/test/resources"
  override fun getTestName(lowercaseFirstLetter: Boolean) =
    super.getTestName(lowercaseFirstLetter).trim()
}

class SimpleParsingTestCase : AyaParsingTestCase("parsing") {
  fun `test prims`() = doTest(true, true)
}
