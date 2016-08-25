引擎定制示例

引擎定制后的使用示例
```java
//获取插件首面数据
MainViewModel viewModel = new MainViewModel();
source.getNodeViewModel(viewModel, source.home, isUpdate, (code) -> {
    if (code == 1) {
        DoBindingView();
    }
});

//获取分类数据
TagViewModel viewModel = new TagViewModel();
source.getNodeViewModel(viewModel, false, 1, tagUrl, source.tag(tagUrl), (code) -> {
    if (code == 1) {
        DoBindingView();
    }

});

//获取书的数据
BookViewModel viewModel = new BookViewModel();
source.getNodeViewModel(viewModel, isUpdate, bookUrl, source.book(bookUrl), (code) -> {
    if (code == 1) {
        DoBindingView();
    }
}

//获取章节的数据
SectionViewModel viewModel = new SectionViewModel();
source.getNodeViewModel(viewModel, false, sectionUrl, source.section(sectionUrl), (code) -> {
    if (code == 1) {
        DoBindingView();
    }
}

```
