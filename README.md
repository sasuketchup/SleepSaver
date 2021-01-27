# SleepSaver
睡眠を管理するために就寝時刻と起床時刻を記録するアプリです。

## 動作環境
Android (API Level 15 以上)

## 使い方
### メイン画面
<img src="https://user-images.githubusercontent.com/33775868/104998010-e1227900-5a6d-11eb-9568-5f15a5f95acc.png" width="240px">

#### 時刻を記録する
1. 「起床時刻」、「就寝時刻」ボタンで時刻を記録するダイアログを表示します。
2. ピッカーを操作して時刻を選択し、「記録する」ボタンで記録します。
<img src="https://user-images.githubusercontent.com/33775868/105794488-783b8380-5fce-11eb-9000-c76ecd338a5d.png" width="240px">

#### 記録を修正する/未入力の欄に後から記録する/記録を削除する
1. 修正または削除したい欄の記録を長押しすると、時刻を修正するダイアログを表示できます。
2. 「[時刻を記録する](#時刻を記録する)」と同様にピッカーを操作して「修正する」ボタンで修正、または「削除する」ボタンで削除します。
<img src="https://user-images.githubusercontent.com/33775868/105795268-e46ab700-5fcf-11eb-98f2-47a7961fd394.png" width="240px">

#### 新しい入力欄の挿入
* アプリを開いた際、前回記録した時から「[就寝→起床切り替え時刻](#就寝起床切り替え時刻)」を跨いでいれば、新しい入力欄が自動的に挿入されます。
* 日付を何日も跨いでいた場合でも、その分の入力欄が一度に挿入されます。
* 「就寝→起床切り替え時刻」は設定画面で変更可能です。
<img src="https://user-images.githubusercontent.com/33775868/105794795-17607b00-5fcf-11eb-926e-65aecdf12638.png" width="240px">

#### 睡眠時間を確認する
* 就寝時刻とその翌日の起床時刻が揃っていれば、睡眠時間を自動で計算し表示します。
<img src="https://user-images.githubusercontent.com/33775868/105794953-5f7f9d80-5fcf-11eb-9e01-4fb63fa88acb.png" width="240px">

#### 各画面へ遷移する
* 「[睡眠データ](#睡眠データ画面)」、「[グラフ](#グラフ画面)」、「[設定](#設定画面)」ボタンでそれぞれの画面へ遷移します。

### 「睡眠データ」画面
* 「睡眠データ」画面では、期間を指定して記録の平均値、目標達成回数と達成率を確認できます。
<img src="https://user-images.githubusercontent.com/33775868/105952881-97f9a700-60b5-11eb-8b2d-e8e43675267e.jpg" width="240px">

#### 対象期間の変更
1. 「期間:○/○/○〜○/○/○」という表示を押すと対象期間を選択するダイアログが表示されます。
<img src="https://user-images.githubusercontent.com/33775868/105953067-e8710480-60b5-11eb-80df-f4d6c622747d.jpg" width="240px">

2. 表示したい範囲を選択します。「指定日〜今日」と「指定日1〜指定日2」を選択するとカレンダーのダイアログが表示され、日付を選択できます。
<img src="https://user-images.githubusercontent.com/33775868/105953212-240bce80-60b6-11eb-9975-dd1835b3f385.jpg" width="240px">

3. 対象期間を変更した状態でメイン画面に戻ろうとするとダイアログが表示され、期間を設定として保存するか、保存せずに戻るか選べます。保存するとメイン画面の表示件数にも反映されます。
<img src="https://user-images.githubusercontent.com/33775868/105953409-7c42d080-60b6-11eb-8915-d78a434d34e3.jpg" width="240px">

### 「グラフ」画面
* 「グラフ」画面では、タブ操作で3種類のグラフを確認できます。
* 目標時刻のラインも表示され、達成の場合には色が変わります。
#### 「直近2週間」
* 直近2週間の毎日の記録です。塗りつぶされた領域は睡眠時間を表します。
<img src="https://user-images.githubusercontent.com/33775868/105953559-b7dd9a80-60b6-11eb-8011-8cd3b943f8f7.jpg" width="240px">

#### 「週毎の平均」
* 1週間の平均を直近の12週間表示します。
<img src="https://user-images.githubusercontent.com/33775868/105953713-f96e4580-60b6-11eb-8e44-63001916296a.jpg" width="240px">

#### 「月毎の平均」
* 過去1年間の1ヶ月毎の平均のグラフです。
<img src="https://user-images.githubusercontent.com/33775868/105953819-2d496b00-60b7-11eb-85d7-a7510ffbcb98.jpg" width="240px">

### 設定画面
* 各種設定を変更し保存できます。
#### 記録の表示
##### 表示範囲
* メイン画面に表示する記録の範囲及び「睡眠データ」画面の対象期間に反映されます。
* [「睡眠データ」画面から変更する場合](#対象期間の変更)と同様に、カレンダーのダイアログから日付を指定することもできます。
#### 1日のサイクル
##### 起床→就寝切り替え時刻
* 正午にあたる時刻を設定できます。日付を跨いだかの判定の際などに、「[就寝→起床切り替え時刻](#就寝起床切り替え時刻)」と合わせて参照されます。「就寝→起床切り替え時刻」を設定する際、例えば23時は0時より前、1時は0時より後という区別ができません(どちらも0時より後となるため)。この時刻を設定することで、「起床→就寝切り替え時刻」から0時までの間であれば0時より前、0時から「起床→就寝切り替え時刻」の間であれば0時より後というように区別ができます。
##### 就寝→起床切り替え時刻
* 日付を変更する時刻を設定できます。アプリを開いた際、この時刻を基準に日付を跨いだか判定し、入力欄の挿入を行います。例えば0時より遅く「[起床→就寝切り替え時刻](#起床就寝切り替え時刻)」より早い時刻に設定した場合、0時以降にアプリを開いても設定した時刻になるまでは次の日の入力欄は挿入されません。
#### 睡眠目標
##### 目標就寝時刻
* 目標就寝時刻を設定できます。この時刻より就寝時刻が早ければ達成です。
##### 目標起床時刻
* 目標起床時刻を設定できます。この時刻より起床時刻が早ければ達成です。
##### 目標睡眠時間
* 目標睡眠時間を設定できます。この時間より睡眠時間が長ければ達成です。目標起床時刻と目標就寝時刻の差か自分で時間を指定するかを選択できます。
#### 押し忘れ入力時の初期表示時刻
* 未入力の欄に後から記録する際、初めに表示される時刻を設定できます。現在時刻（押した時の時刻）、前日の記録、過去1週間の平均、自分で指定の中から選択できます。就寝・起床それぞれについて設定できます。
#### 充電切り替え時のポップアップ画面表示
* 端末を充電器に接続または切断した際に[記録画面](#充電接続切断時の記録画面の表示)を表示するかどうか選択できます。就寝前に充電器に接続し起床後に切断する場合などに有効で、手動でアプリを起動させなくても記録を行うことができます。
* 初期状態では接続時に就寝時刻、切断時に起床時刻の記録画面を表示する設定になっていますが、反転させることもできます。
#### 設定の保存
* 「保存する」ボタンを押すとすべての設定を保存し、メイン画面に戻ります。
* また、設定を変更した状態で保存せずメイン画面に戻ろうとすると、変更を破棄してよいか確認するダイアログが表示されます。

### 充電接続・切断時の記録画面の表示
* 設定で「[充電切り替え時のポップアップ画面表示](#充電切り替え時のポップアップ画面表示)」の「記録画面を表示する」をONにしている場合、端末を充電器に接続した際、または充電器から切断した際に記録画面が表示されます。就寝前に充電器に接続し起床後に切断する場合などに有効です。
<img src="https://user-images.githubusercontent.com/33775868/105790925-81c1ed00-5fc8-11eb-919e-83e6f8bf9c8c.jpg" width="240px">

#### 時刻を操作する/現在時刻に合わせる
* この画面では、充電器に接続または切断し画面が表示された時刻が初期値として表示されます。その初期値が記録したい時刻とずれた場合、[メイン画面から記録する際](#時刻を記録する)と同様にピッカーを操作して時刻を合わせられます。
* また、「現在時刻にセット」ボタンを押せば、ピッカーの時刻がボタンを押した時刻に合わせられます。
#### 時刻を記録する
* 「記録する」ボタンを押すと、ピッカーに表示されている時刻が記録されます。初期状態では接続した場合は就寝、切断した場合は起床時刻を記録しますが、[設定画面で反対にする](#充電切り替え時のポップアップ画面表示)こともできます。画面の表示でどちらの時刻を記録するのか確認できます。
* 「記録する」ボタンを一度押すとボタンの文字が灰色になり押せなくなります。これによって、記録したということが確認できます。再度充電器に接続・切断して画面を表示すると「記録する」ボタンも押せるようになります。
#### アプリ画面を開く
* この画面からアプリのメイン画面を開くには、「アプリを開く」ボタンを押します。端末のロックを解除するとメイン画面が表示され、「記録する」ボタンを押していれば、時刻が記録されていることを確認できます。
#### 記録画面を閉じる
* 「閉じる」ボタンで記録画面を閉じることができます。

## インストール
