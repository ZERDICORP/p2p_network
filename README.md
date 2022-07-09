# p2p_network 🕸️ 
#### Oh, I've been wanting to play with P2P networks for so long!

## What is it? :eyes:
Well, the other day I watched the series **Silicon Valley**, in which a group of enthusiasts developed a compression algorithm.  
But I was attracted not so much by the compression algorithm as by its application, one of which was a decentralized data warehouse.  
And then I remembered that I had long wanted to create such a network. And then... That's right, then I started to implement it.

***Did I succeed?*** Let's just say I'm not disappointed.  
The network turned out to be working. Files are sharded, shards are encrypted and distributed between nodes.  
Everything is like in the series.

***Should you use this network?*** Oh no!  
My implementation of a decentralized p2p network is just an introductory project with no expectation of mass use (to be honest, the project smacks of various optimization problems).

***Why didn't I solve the problem?*** Everything is simple - it makes no sense.  
My whole interest was to figure out how such a network functions, how nodes learn about each other, how they communicate, and so on.  
And things like query parallelization and other optimizations are simply superfluous, since the project is just for fun, besides, the network was tested on only 3 nodes, so the performance that we have now is quite enough.

## How it works? :hand:
> Before reading further, let's agree on the meanings of the following words:  
> **node** - *java program; socket server.*

### «Acquaintance»
After launching a new node, it is not yet in the p2p network.  
To get there, it needs to tell all other nodes that it exists.  
To do this, it needs to send some notification to one of the network nodes (such a node can also be called the root node).  
After notifying the root node, all other nodes will be notified that a new node has joined the network.

### «Cloning nodes»
As soon as the whole network knows about the existence of a new node, it adds the address of this node to the "address book".  
Thus, this node becomes a member of the network.  
But what about the newest node? What about his address book? How can he find out about the nodes of the network?  
Here, the node must request an "address book" from the root node.

### «Data sharing»
Now that the new node knows about the other nodes, it can exchange data between them.  
That is, the owner of a node can ask that node to store some data on the network.  
The node will then make multiple copies of this data and send each copy to a different node, providing storage and backup.

### «Anything else»
All of the above is the simplest version of this kind of network. But the idea is conveyed correctly.  
As an addition, you can implement data sharding (breaking into small segments) and encrypting them.

## How to become a member of the network? :couple:
> It's important to note that although the repository is referred to as a "p2p network", it's actually just one node.  
> And the network will be reached when the number of nodes > 1, simultaneously working and connected with each other.

#### 1. Clone the repository
```
$ git clone https://github.com/ZERDICORP/p2p_network.git
```

#### 2. Use maven
```
$ cd p2p_network
$ mvn clean compile assembly:single
```

#### 3. Start node
> Below, my server acts as the root node.
```
$ echo "java -jar target/p2p_network-1.0-SNAPSHOT-jar-with-dependencies.jar \$@" > p2p && chmod +x p2p
$ ./p2p -s 188.187.188.37
[log]: server has been started on port 8080..
```

#### 4. Сlient usage
> When executing each command on the client, you will need to enter a secret (it can be different for each file).
+ **Saving a file**
> The contents of the file "file.txt":
> Hello, world!
```
$ ./p2p -c save /path/to/file.txt
[>] enter secret: 
[log]: file saved successfully!
```
+ **Getting a file**
```
$ ./p2p -c cat file.txt
[>] enter secret: 
Hello, world!
```
> Or if you want the result to be written to a file:
```
$ ./p2p -c cat file.txt -o out.txt
[>] enter secret:
[log]: file contents are written to: out.txt
```
+ **Renaming a file**
```
$ ./p2p -c mv file.txt newFileName.txt
[>] enter secret: 
[log]: file renamed successfully!
```
+ **Deleting a file**
```
$ ./p2p -c rm file.txt
[>] enter secret:
[log]: file deleted successfully!
```
