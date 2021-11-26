# Solver for primary numbers


# Задача
1. Построить машину Тьюринга (*МТ*) и линейный ограниченный автомат (*LBA*), допускающие язык **L** простых чисел в унарной системе счисления.
2. Далее:
    1. Для МТ написать транслятор в КС грамматику, которая порождает тот же язык **L**.
    2. Для LBA написать транслятор в КЗ грамматику, которая порождает тот же язык **L** (в процессе, это задание Любви Милосердовой)
3. Написать программу, которая принимает число, говорит простое оно или нет и выводит дерево разбора в файл в случае, если простое

# Решение
1. Файлы `prime_tm.txt` и `prime_lba.txt` для МТ и LBA соответственно.
2. Файл `Converter.kt`
3. Файл `DerivationBuilder.kt`

## Запуск
#### Неограниченная грамматика
```bash
kotlinc $(find . -name "*.kt") -include-runtime -d main.jar
java -jar main.jar T0
```


Будет приглашение для ввода. Вводим число в десятичной системе счисления (внутри оно переведется в унарную систему, десятичная была выбрана при вводе для удобства). 
В итоге выведется либо **Yes, number is prime**, при этом произойдет запись в один из файлов в `res/derivations` (в зависимости от типа грамматики), если введенное слово выводимо в данной КС грамматике и **No, number is not prime**, если нет.

## Выход из программы
```bash
> quit
```

### Вывод продукций
В одном из файлов (в зависимости от типа грамматики) в папке **res/derivations** сохраняется последний удачный вывод простого числа в следующем виде:
каждая строка - состояние ленты перед применением одной продукции.

Начальная строка представляет заданное слово на ленте (со всеми спец символами). Далее следует последовательное применение продукций, пока не достигнется финальное состояние (**finish** или **qaccept**), и последующий привод ленты к исходному числу.

Последняя строка состоит из терминалов, представляя заданное простое число

#### Пример вывода простого числа 2 из Т0
```bash
(epsilon|_), (epsilon|_), (epsilon|_), (epsilon|_), q00, (1|1), (1|1), (epsilon|_), (epsilon|_)
(epsilon|_), (epsilon|_), (epsilon|_), q01, (epsilon|_), (1|1), (1|1), (epsilon|_), (epsilon|_)
(epsilon|_), (epsilon|_), (epsilon|_), (epsilon|#), q0, (1|1), (1|1), (epsilon|_), (epsilon|_)
(epsilon|_), (epsilon|_), (epsilon|_), (epsilon|#), (1|1), q1, (1|1), (epsilon|_), (epsilon|_)
...
1, 1, finish, finish, finish
1, 1, finish, finish
1, 1, finish
1, 1
```