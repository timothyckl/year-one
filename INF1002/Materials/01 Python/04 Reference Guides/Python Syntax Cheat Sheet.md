# Python Syntax Cheat Sheet

Quick lookup for everyday Python syntax used in INF1002.

## Printing and strings

```python
print("Hello World!")          # quotes interchangeable
print('It\'s a day')           # escape a quote
print(r"C:\Users\Name")        # raw string: no escapes
print("line1\nline2\tTAB")     # \n newline, \t tab
print("a", "b", "c")           # space separated args
```

## Comments

```python
# single line comment
''' multi-line comment / docstring '''
```

## Variables

```python
name = "Taylor"        # assignment
x, y = 10, 20          # multiple assignment / unpacking
temp = x; x = y; y = temp   # swap
type(x)                # -> <class 'int'>
id(x)                  # memory address
```

## Numbers and operators

```text
a + b  a - b  a * b  a / b      # / always float
a // b                          # floor division
a % b                           # modulus
a ** b                          # exponent
a += 1   # == a = a + 1         # also -= *= /=
int("5")   float("5.7")   str(5)   # casts (int() truncates, not rounds)
```

## Conditions

```python
if x == 5:
    ...
elif a < b:
    ...
else:
    ...

# truthiness: 0, None, "", [], {}, set(), range(0) are falsy
if x is None: ...
if [1,2,3]: ...          # non-empty list is truthy
value = "yes" if flag else "no"   # conditional expression

match drink:              # Python 3.10+
    case "Latte":
        ...
    case _:
        ...
```

## Loops

```python
while condition:
    statement

for item in sequence:     # list, tuple, string, range
    statement

for i in range(5):        # 0..4
for i in range(2, 10):    # 2..9
for i in range(2, 10, 2): # 2,4,6,8

break        # exit loop
continue     # skip to next iteration
# for...else runs if loop completed without break
```

## Functions

```python
def add(x, y):            # definition
    '''docstring'''
    return x + y

def f(a, b=10, *args, **kwargs): ...   # default, variadic

# immutable args (int/str/float/tuple) are NOT changed by the call
# mutable args (list/dict) ARE changed by the call
```

## Main guard

```python
if __name__ == "__main__":
    main()                # runs only when executed directly
```

## List / tuple / dict / set

```python
lst = [1, 2, 3]
lst.append(4)             # add at end
lst.insert(0, 0)          # add at index
lst[1] = 99               # update
del lst[0]                # delete by index
lst.remove(99)            # delete by value
len(lst)  max(lst)  min(lst)  sum(lst)

tup = (1, 2, 3)           # immutable
d = {"a": 1}
d["b"] = 2                # add/update
del d["a"]
d.keys()  d.values()  d.items()  d.update(other)
for k, v in d.items(): ...

s = {1, 2, 3}             # set
```

## Comprehensions

```python
[i**2 for i in range(10)]
[x for x in range(20) if x % 2 == 0]
[func(x) for x in data]
{expr for x in it}                # set
{k: v(k) for k in it}             # dict
```

## Higher-order tools

```python
sorted(data, key=abs)                       # custom key
list(map(square, data))                     # lazy -> list
list(filter(is_even, data))                 # lazy -> list
from functools import reduce                # py3: not builtin
reduce(lambda a, b: a + b, data)
lam = lambda x: x * 2                       # anonymous fn
doTwice = lambda f, x: f(f(x))
```

## Formatting

```python
f"Average:{avg:.2f}"        # 2 decimals
f"{name:>10}"               # right-align width 10
f"{name:<10}"               # left-align
f"{name:^10}"               # center
"%0.2f" % bmi               # %-style 2 decimals
"%5d" % n                   # integer width 5
```

## Common errors to remember

- `if x = 5:` -> SyntaxError (use `==`).
- `int('5.7')` -> ValueError.
- `10 / 0` -> ZeroDivisionError.
- Reassigning a global inside a function without `global` -> UnboundLocalError.
- Copying a list with `=` shares the object; use `list[:]`, `copy.copy`, or
  `copy.deepcopy`.
- Missing base case in recursion -> RecursionError.
- `quit()`/`exit()` in Gradescope submissions -> test failure (use `return`).
