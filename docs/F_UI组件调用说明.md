# F UI 通用组件调用说明

## 1. 普通提示框

用于未登录、网络失败、列表为空等只有一个“确定”按钮的提示。

```java
DialogUtil.showHintDialog(context, "请先登录", false);
```

第三个参数表示点击确定后是否退出当前 Activity：

- `false`：只关闭弹窗
- `true`：关闭弹窗并结束当前 Activity

## 2. 下单确认框

C 的下单模块点击菜品后调用：

```java
DialogUtil.showDecideDialogWithTitle(
        context,
        "是否确认下单",
        name + "：" + price + "元",
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.dismissDialog();
            }
        },
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.dismissDialog();
                // 调用 OrderHelper.order(...)
            }
        }
);
```

## 3. 无标题确认框

E 的退出登录等场景可以调用：

```java
DialogUtil.showDecideDialogNoTitle(
        context,
        "确认退出登录？",
        cancelListener,
        confirmListener
);
```

## 4. 加载框

B、C、D 进行网络请求前显示加载框，请求成功或失败后必须关闭。

```java
LoadingDialogUtil.showLoadingDialog(context);

// success/fail 回调里都要调用
LoadingDialogUtil.cancelLoading();
```

## 5. 错误日志

网络失败、JSON 解析失败、数据库响应异常等场景调用：

```java
LogUtil.logErr("请求失败：" + failMsg);
```

## 6. 当前注意事项

- `DialogUtil`、`LoadingDialogUtil`、`LogUtil` 均位于客户端包 `com.example.sec_android` 下。
- 弹窗布局文件位于 `app/src/main/res/layout/`：
  - `dialog_hint.xml`
  - `dialog_decide.xml`
  - `dialog_loading.xml`
- 其他组员调用前需要确认已合并 F 的组件文件和对应 XML 布局。
