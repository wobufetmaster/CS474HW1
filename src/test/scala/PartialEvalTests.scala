import MySetTheoryDSL.*
import MySetTheoryDSL.Fields.*
import MySetTheoryDSL.argExp.*
import MySetTheoryDSL.assignRHS.*
import MySetTheoryDSL.bExp.*
import MySetTheoryDSL.classBodyExp.*
import MySetTheoryDSL.classExp.*
import MySetTheoryDSL.inheritanceExp.*
import MySetTheoryDSL.setExp.*
import org.scalatest.funsuite.AnyFunSuite

class PartialEvalTests extends AnyFunSuite {
  
  test("Basic Partial Eval Test") {

    assert(Variable("undefined").eval() == Variable("undefined"))

    val s = Insert(Variable("no"),Variable("nada"),Variable("nil"))

    assert(s == s.eval())

    Assign("total_eval",Set(Value(4))).eval()

    assert(Check("total_eval",Value(4)))



  }

  test("More complicated partial eval") {


  }

  test("Optimization 1 test") {
    val s = Insert(Union(Value("cat"),Value("dog")),Variable("Undefined"))
    assert(s.eval() == Insert(Value(("cat","dog")), Variable("Undefined")))

    val r = Insert(
      Difference(
        Insert(Value(1),Value(2),Value(3)),
        Insert(Value(2),Value(3),Value(4))), Variable("Undefined"))

    assert(r.eval() == Insert(Value(1)),Variable("Undefined"))
  }
  test("Optimization 2 test") {

  }
  test("Optimization 3 test") {

  }
  test("All optimizations together") {

  }

  
}