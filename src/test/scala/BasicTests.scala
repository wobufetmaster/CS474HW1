import MySetTheoryDSL.*
import MySetTheoryDSL.setExp.*
import MySetTheoryDSL.classExp.*
import MySetTheoryDSL.classBodyExp.*
import MySetTheoryDSL.argExp.*
import MySetTheoryDSL.assignRHS.*
import MySetTheoryDSL.Fields.*
import org.scalatest.funsuite.AnyFunSuite
import MySetTheoryDSL.inheritanceExp.*

class BasicTests extends AnyFunSuite {
  
  test("Basic Methods Test") {
    ClassDef("dog",Extends(None),Constructor(),Method("eat",Args(),Insert(Value("dog food")))).eval()

    Assign("my_dog",NewObject("dog")).eval()
    Assign("my_food",Set(InvokeMethod("my_dog","eat"))).eval()
    assert(Check("my_food",Insert(Value("dog food"))))
  }
  
  test("Basic Fields and Constructors Test") {
    ClassDef("dog",Extends(None),Constructor(AssignField(This(),"tail_size",Insert(Value(4)))),Field("tail_size")).eval()

    Assign("my_dog",NewObject("dog")).eval()
    Assign("my_tail",Set(GetField("my_dog","tail_size"))).eval()
    assert(Check("my_tail",Insert(Value(4)))) //Default value for constructor

    AssignField(Object("my_dog"),"tail_size",Value(8)).eval() //Tail extension
    Assign("my_tail",Set(GetField("my_dog","tail_size"))).eval()
    assert(Check("my_tail",Insert(Value(8)))) //Should have new value

  }
  
}
