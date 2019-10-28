# 一个生成Golang struct的插件
### 用法
1. 在需要生成struct的文件中右键，generate -> json2go
2. 在输入框中输入json
3. 在root type中输入根struct名
4. 点击ok

### 注意
1. 每个struct的名称跟json中的名称一致，首字母转大写，**只有根节点的需要手动指定**。
2. struct的每个字段跟json中名称一致，首字母转大写，后续版本开放自定义struct名。
3. 当前struct均为指针，后续版本开放自定义指针。
4. 如果出现同名struct，插件将会把父struct名称和struct名称拼接在一起。
