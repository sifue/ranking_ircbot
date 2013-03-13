# Ranking Ircbot
IRCの発言ランキングを作成するボット

# ビルド方法
javaとsbtをインストールの上、

```sh
$ sbt
> assembly
```
これでtargetディレクトリの中に、ranking_ircbot-assembly-2.0.jarがビルドされます。

# 使い方
ranking_ircbot-assembly-2.0.jarと同じディレクトリに、
ranking_ircbot_template.propertiesを正しく編集して、
ranking_ircbot.propertiesというファイル名で保存ください。

```properties
irc.address = hostname
irc.channel = #channelname
irc.nickname = ranking_ircbot
irc.charset = UTF-8

設定の後、

```sh
$java -jar ranking_ircbot-assembly-1.0.jar
```

で実行することができます。
またIRCのボットが入っているチャンネルにて
```
ping nickname
```
とするとWorking now.とnoticeを返します。

