# Revision - Functions and Modules

## Function definition

```python
def function_name(arguments):
    '''docstring'''
    body
    return [expression]   # optional
```

- Name, arguments (input), docstring (comment), suite (body), return value.
- Return exits the function and passes a value back to the caller.
- Number of arguments at the call site must match the definition.

## Argument kinds

- **Formal arguments**: names used inside the function.
- **Actual arguments**: real values passed at the call.
- **Positional**: matched by order. `open('f', 'r', 'utf-8')`.
- **Keyword**: matched by name, order irrelevant. `open('f', 'r',
  encoding='utf-8')`.
- **Default arguments**: give a parameter a default value so it can be omitted.
- `*args`: packs any number of positional args into a tuple.
  ```python
def sum_numbers(*args):
    return sum(args)
```
- `**kwargs`: packs keyword args into a dict.
- Positional-only / keyword-only parameters: robustness when there are many
  parameters (advanced topic).

## Do function calls change the caller's variables?

- **Immutable** (str, int, float, tuple): NO. `average += 10` inside the
  function rebinds the local name to a new object; the caller's variable keeps
  its original value and id.
- **Mutable** (list, dict): YES. `scores.append(10)` mutates the shared object;
  the caller sees the change and the id is unchanged.
- This is the single most tested function concept in the module.

## Scope

- Local variable: defined inside a function; alive only while it runs.
- Global variable: defined at module level; readable inside functions.
- Reassigning a global inside a function without `global` raises
  `UnboundLocalError: local variable 'marks' referenced before assignment`.
- `nonlocal` (advanced): refers to a variable in an enclosing (nested) scope.

## Annotations / type hints

- `def f(x: int) -> str: ...` annotates params and return type.
- Improves readability and IDE support, enables type checking; does not change
  runtime behaviour.

## Modules

- A module is a `.py` file with functions/classes/constants.
- `import module_name` (no `.py`); call `module_name.func()`.
- `import module_name as alias`.
- `from module_name import func1, func2` - call directly; saves loading time
  for big modules.
- `from module_name import *` imports all public names.
- `help(module_name)` shows docstrings; `dir(module_name)` lists names.
- Module docstring = first statement of the file; function docstring = first
  statement of the function body.
- `if __name__ == "__main__":` guards code that should only run when the file
  is executed directly (not when imported).

## os.path helpers

`os.path.join`, `os.path.split`, `os.path.splitext`, `os.path.exists`,
`os.mkdir`.

## Hierarchy

`Library -> Package -> Module (module1.py, module2.py, Subpackage -> ...)`

## Common built-in modules

`math`, `datetime`, `random`, `os`, `urllib2`.

## Quick self-check

1. Why does `x` stay 10 after `f(x)` when `f` does `x += 1`? (int is
   immutable; `+=` rebinds the local name.)
2. Why does a list change after being passed to a function that `append`s? 
   (list is mutable; the function mutates the shared object.)
3. `*args` gives a tuple; `**kwargs` gives a dict.
4. What does `help(myMath)` need to show docs? A module docstring at the top of
   `myMath.py` and per-function docstrings.
