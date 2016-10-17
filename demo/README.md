引擎定制后的使用示例

::1.实例化插件引擎
```java
String sited = HttpUtil.get("http://x.x.x/xxx.sited.xml");//或者从本地加载
DdSource source = new DdSource(App.getCurrent(), sited);
```

::2.使用插件引擎获取数据
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
source.getNodeViewModel(viewModel, false, viewModel.currentPage, tagUrl, source.tag(tagUrl), (code) -> {
    if (code == 1) {
        DoBindingView();
    }
});

//获取搜索结果数据
source.getNodeViewModel(viewModel, false, key, 1, source.search, (code) -> {
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
});

//获取章节的数据
SectionViewModel viewModel = new SectionViewModel();
source.getNodeViewModel(viewModel, false, sectionUrl, source.section(sectionUrl), (code) -> {
    if (code == 1) {
        DoBindingView();
    }
});

```
