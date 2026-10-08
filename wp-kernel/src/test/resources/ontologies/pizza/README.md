# pizza.owl（测试本体）

kernel 验收测试（05 文档 P0-06、P0-07、P0-09）和 `wp-legacy-compat` 新旧内核对比测试使用的本体。

- 这是为本项目编写的测试本体，沿用经典 pizza 教程本体的结构（命名比萨、配料、饼底、定义类、值分区、枚举的国家类），但不是 Manchester 的 pizza.owl 原文。
- 规模：60 个类、8 个对象属性、2 个数据属性、377 条公理（188 条逻辑公理），RDF/XML 格式。
- 覆盖的构造：多层子类、`EquivalentClasses`（交、并、枚举）、存在/全称/基数/hasValue 限制、不相交类、属性的逆/传递/函数性、子注解属性、个体与否定属性断言，以及 `owl:deprecated`（`Veneziana`）。
- 标签：所有类都有 `rdfs:label@en`，部分有 `@zh`（如 `Pizza` → “比萨”），`Pizza` 另有 `skos:prefLabel`、`skos:altLabel`。

需要换成原版 pizza.owl 时，直接替换本文件，并同步修改 `PizzaOntology` 里的公理数和各测试中的期望值。
