# Ranking Ircbot
IRCの発言数ランキングと名言の登録、及び登録数のランキングをチャンネルごとに設定できるIRCのボットです。
複数のチャンネルに参加し、kickされても戻ってきたり、
オペレーター権限を与えられている場合、他の参加者にオペレーター権限を分け与えたりする機能があります。
なお、IRCの代わりのSlackのAPIを通じて投稿する機能もあります。

# ビルド方法
java(jdk6以上)とsbt(0.13.5)をインストールの上、

```sh
$ sbt
> assembly
```
以上を実行することで、targetディレクトリの中に、ranking_ircbot-assembly-2.X.jarがビルドされます。

# 使い方
ranking_ircbot-assembly-3.X.jarと同じディレクトリに、
ranking_ircbot_template.propertiesを正しく編集して、
ranking_ircbot.propertiesというファイル名で保存ください。
さらに、ranking_ircbot_empty.h2.dbを
ranking_ircbot.h2.dbという名前に変更してください。

```properties
irc.address = hostname
irc.channel = #channelname1 #channelname2
irc.nickname = ranking_ircbot
irc.username = ranking_ircbot
irc.password =
irc.port = 6667
irc.use_ssl = false
irc.charset = UTF-8
irc.use_slack_post = false
db.url = jdbc:h2:file:ranking_ircbot
db.driver = org.h2.Driver
twitter.enable = false
twitter.consumer_key =
twitter.consumer_secret =
twitter.access_token =
twitter.access_token_secret =
slack.username = ranking_ircbot
slack.token = xoxp-99999999-99999999-99999999-aaaaaa
slack.icon_url = https://pbs.twimg.com/profile_images/480515573743230976/ZMQlSasz.jpeg
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
dailyranking>
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
そのチャンネルで{nickname}の名言を保存、スペースを含む発言は覚えられないので、全角スペース等に置き換えて下さい

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

```
@{screen_name}[ {0~19の整数}]
```
twitterのクライアントの設定をtrueにして、twitterアプリケーションの各種キーを設定すると、
screen_name: {ツイートの内容}
という形式で、screen_nameのツイッターのユーザーの0~19番目のインデックスの発言をは参照することができます。
スペース{0~19の整数}を入力しない場合は最も最近のつぶやきがつぶやかれます。