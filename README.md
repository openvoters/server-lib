OpenVoters.Org
==============

What is it?
------------
Opervoters is a free, open source framework that allows to write polls systems based on Mobile apps for iOS, Android and Windows Phone. Being open source is key to let everybody see how the votes are counted, even though a security mechanism will be used by each poll application to guarantee that the voting APIs are not maliciously abused.

How it works?
-------------
The vote are anonymous, each mobile device can vote once. From the same device you can change your mind, but you cannot vote twice. If the voter allows it, the vote can be shared on Twitter or Facebook, to spread the word about the poll in progress.

The framework is composed by a server library and one client library per each platform supported:

- server side, the system offers a set of REST APIs and some pre-cooked web views to manage the votes database and to quickly get the poll's outlook.
- client side, the libraries allows to quickly build an app for the poll you want to implement.
