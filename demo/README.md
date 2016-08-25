引擎定制示例

引擎定制后的使用示例
```java
//获取插件首面数据
source.getNodeViewModel(viewModel, source.home, isUpdate, (code) -> {
    if (code == 1) {
        DoBindingView();
    }

    HintUtil.hide(activity);

});

//获取分类数据
tagConfig = source.tag(tagUrl);
tagConfig.url = tagUrl;
source.getNodeViewModel(viewModel, false, 1, tagConfig, (code) -> {
    if (code == 1) {
        listView.setAdapter(viewAdapter.bind(viewModel.resultList));
    }

    HintUtil.hide(activity);

});

```
