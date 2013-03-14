# Ranking Ircbot
IRCの発言数ランキングと名言の登録、及び登録数のランキングをチャンネルごとに設定できるIRCのボットです。
複数のチャンネルに参加し、kickされても戻ってきたり、
オペレーター権限を与えられている場合、他の参加者にオペレーター権限を分け与えたりする機能があります。

# ビルド方法
java(jdk6以上)とsbt(0.12.2)をインストールの上、

```sh
$ sbt
> assembly
```
以上を実行することで、targetディレクトリの中に、ranking_ircbot-assembly-2.X.jarがビルドされます。

# 使い方
ranking_ircbot-assembly-2.X.jarと同じディレクトリに、
ranking_ircbot_template.propertiesを正しく編集して、
ranking_ircbot.propertiesというファイル名で保存ください。
さらに、ranking_ircbot_empty.h2.dbを
ranking_ircbot.h2.dbという名前に変更してください。

```properties
irc.address = hostname
irc.channel = #channelname1 #channelname2
irc.nickname = ranking_ircbot
irc.charset = UTF-8
db.url = jdbc:h2:file:ranking_ircbot
db.driver = org.h2.Driver
```
以上のようにチャンネルはスペース区切りで複数設定することができます。
日本語のチャンネル名は、[native2asciiのwebサービス](http://lithium.homedns.org/~shanq/bitsnbytes/native2ascii_en.html)などを利用して
入力することができます。

設定の後、

```sh
$java -jar ranking_ircbot-assembly-2.X.jar
```

で実行することができます。
またIRCのボットが入っているチャンネルにて
```
ping bot_nickname
```
とするとWorking now.とnoticeを返して動作確認をすることができます。

# 使い方
ranking_ircbotが参加しているチャンネルにて以下のメッセージを打つと様々な機能が利用できます。

```
hourlyranking>
```
1時間の発言数ランキングの表示

```
daylyranking>
```
24時間の発言数ランキングの表示

```
weeklyranking>
```
1週間の発言数ランキングの表示

```
monthlyranking>
```
30日間の発言数ランキングの表示

```
yearlyranking>
```
1年間の発言数ランキングの表示

```
覚えろ:{nickname} {message}
```
そのチャンネルで{nickname}の名言を保存

```
{nickname} 曰く
```
そのチャンネルで保存している{nickname}の名言をランダムで発言

```
消して:{nickname} {message}
```
そのチャンネルで{nickname}で発言登録された{message}を全て削除

```
wiseranking>
```
そのチャンネルで保存されている名言の個数のランキングを表示


```
ping {ircbot_nickname}
```
Working now. > {nickname} https://github.com/sifue/ranking_ircbot と発言します。