# CS474HW1
##Set theory DSL for CS 474

##Building and Install:
You can install this program from [GitHub](https://github.com/wobufetmaster/CS474HW1). 
This program is buildable using the sbt. It can be run and built using the commands **sbt clean compile test** and **sbt clean compile run** It is also intelliJ friendly, and can be imported into it 
easily. 

You must put the import statements: 
```scala
import MySetTheoryDSL.*
import setExp.*
```
in your scala program in order to use the set theory DSL provided here.

##Basic Syntax:
All expressions need to be evaluated by using the **eval()** method, except for **Check**, which does not require it.
Every expression evaluates to a **Set()**, except for Check, it returns a Boolean. Because of this check cannot be nested inside of other cases, it must be at the top level. 

```scala
Check(Assign,...) //All good
Assign(name, Check(...)) //Compile time error
```
Aside from that, any command that takes a **setExp** as an argument can have any other case of **setExp** used as an argument. Some operations, like **Insert()** and **Assign()** 
accept an unlimited number of **setExp** arguments. In these cases, the arguments are evaluated from left to right. Be aware of this if some of the commands contain side effects, like
**Delete()** or **Macro()**. 
For example: 
```scala
CreateMacro("myMacro",Assign(Variable("myVariable"),Value(3))).eval() //Assigns myVariable to be 3. Remember that assign returns nothing!
Assign(Variable("mySet"),Macro("myMacro"),Variable("myVariable")).eval() //The macro instantiates the variable myVariable, then adds it to the set, all good.
assertThrows[NoSuchElementException](Assign(Variable("mySet"),Variable("myVariable"),Macro("myMacro")).eval()) //myVariable gets added to the set before it's instantiated, which fails.
```


Also note that **Assign()**, **Delete()**, and **CreateMacro()** simply return empty sets. 


The type signature of **Check()** is: 
```scala
Check(set_name: String, set_val: Value, set_scope: Option[String] = None)
```

This checks if the value **set_val** is in the set **set_name**, with optional parameter **set_scope** to determine the scope. If no argument is provided, it defaults to None, representing global scope.

**Assign(name: Variable, op2: setExp\*)** binds a name to the set formed by evaluating each of the set expressions in op2, and combining them together. 
Note that in this language, nested sets are generally avoided, unless specifically created by **Product()**, or **NestedInsert()**, which we will see later.
Therefore, the following statements are equivalent:
```scala
Assign(Variable("someSetName"), Value(1), Value("somestring"), Value(0x0f))
Assign(Variable("someSetName"), Insert(Value(1), Value("somestring")),Value(0x0f))
```
Also note that assigning to a variable that has already been declared in the current scope will overwrite the old value.

**Insert(op: setExp\*)** does essentially the same thing as Assign, but simply returns the set it creates, without binding it to a name. 
Because this does not create nested sets, the following statements are equal. 
```scala
Insert(Value(4))
Insert(Insert(Insert(Value(4)))) 
```
**NestedInsert(op: setExp\*)** This works the same as Insert, except it puts each of its evaluated set expressions in an enclosing set before combining them.
```scala
Assign(Variable("someSetName"), NestedInsert(Value(1), Value("somestring"))) //This should equal Set(Set(1),Set(somestring))
```

**Value(v: Any)** simply returns the value it was given as a set. 

**Variable(set_name: String)** looks up the value of the variable set_name in the current scope, and returns the set it represents, or throws an exception if it does not exist.  
```scala
Assign(Variable("someSetName"), Insert(Value(1)), Value("3"), Value(5))
Assign(Variable("myOtherSet"), Insert(Variable("someSetName"),Value(777777))) //This should be equal to Set(1,"3",5,777777)
```
Note that there is no nesting of sets here. If that is the desired behaviour, then you can simply use NestedInsert:
```scala
Assign(Variable("myOtherSet"), Insert(NestedInsert(Variable("someSetName")), Value(777777))) //Should be Set(Set(1,"3",5),777777)
```
**Delete(Variable(name: String))** removes the value associated with name from the current scope. 
```scala
Assign(Variable("someSetName"), Insert(Value(9999), Value("somestring"))).eval()
Delete(Variable("someSetName")).eval()
assertThrows[NoSuchElementException](Variable("someSetName").eval())
```
##Binary Set Operations
[Binary Set Operations Reference](https://en.wikipedia.org/wiki/Set_theory#Basic_concepts_and_notation)

**Union(op1: setExp, op2: setExp)**
Returns the set union between op1 and op2.

**Intersection(op1: setExp, op2: setExp)**
Returns the set Intersection between op1 and op2.

**Difference(op1: setExp, op2: setExp)**
Returns the set Difference between op1 and op2.

**SymmetricDifference(op1: setExp, op2: setExp)**
Returns the symmetric difference between op1 and op2.

**Product(op1: setExp, op2: setExp)**
Returns the cartesian product between the two sets. 
for example: 
```scala
Assign(Variable("ProductSet"),Product(Insert(Value(1),Value(3)),Insert(Value(2),Value(4)))).eval()
Check("ProductSet", NestedInsert(
  Insert(Value(1),Value(2)),
  Insert(Value(1),Value(4)),
  Insert(Value(3),Value(2)),
  Insert(Value(3),Value(4))))
```
Note that this is one of the only functions that creates nested sets, along with **NestedInsert**
##Macros
There are two operations that are used for macros. 
**CreateMacro(lhs: String, rhs: setExp)** Binds the name on the lhs to the set expression on the rhs. All macros must be created before they can be used. 
The macro is not evaluated when it is created. 
**Macro(m: String)** evaluates the macro with name m, in the current scope.  
For example: 
```scala
CreateMacro("Add 3",Insert(Value(3))).eval()
Assign(Variable("mySet"),Macro("Add 3")).eval()
assert(Check("mySet",Value(3)))
```


##Scopes:


Scopes are implemented using a stack, **current_scope** and a map, **scope_map**. The stack keeps track of the current scope, and the map maps variable names and scopes onto sets.
**scope_map** has type 
```scala
Map[(String,Option[String]), Set[Any]]
```
**current_scope** has type
```scala
current_scope: mutable.Stack[String]
```
To use scopes, use the expression:
**Scope(name: String, op2: setExp)**
The first parameter is the scope name, and the second is the command to be evaluated within the scope (can be another scope). 
Consider the following code: 
```scala
Scope("scope1",Scope("scope2",Assign(Variable("mySetName"),Value("this is the second scope"),Variable("globalSet"),Variable("scope1Set"))))
```

By using Scope("scopename") you are pushing "scopename" onto the stack, which is then used to resolve variable names. If Variable("globalSet") 
is not found in the current scope, we walk up through the stack until we find the first scope that contains the value. Finally, if there is no scope in the stack that matches, 
we check global (represented by **None**) scope, and if there is no matching value we throw an exception.





    