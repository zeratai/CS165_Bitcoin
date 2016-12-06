import java.util.ArrayList;
import java.util.HashMap;

/* Block Chain should maintain only limited block nodes to satisfy the functions
   You should not have the all the blocks added to the block chain in memory 
   as it would overflow memory
 */

public class BlockChain {
   public static final int CUT_OFF_AGE = 10;
   
   private ArrayList<BlockNode> heads;  
   private HashMap<ByteArrayWrapper, BlockNode> H;
   private int height;
   private BlockNode maxHeightBlock;    
   private TransactionPool txPool;

   // all information required in handling a block in block chain
   private class BlockNode {
      public Block b;
      public BlockNode parent;
      public ArrayList<BlockNode> children;
      public int height;
      // utxo pool for making a new block on top of this block
      private UTXOPool uPool;

      public BlockNode(Block b, BlockNode parent, UTXOPool uPool) {
         this.b = b;
         this.parent = parent;
         children = new ArrayList<BlockNode>();
         this.uPool = uPool;
         if (parent != null) {
            height = parent.height + 1;
            parent.children.add(this);
         } else {
            height = 1;
         }
      }

      public UTXOPool getUTXOPoolCopy() {
         return new UTXOPool(uPool);
      }
   }

   /* create an empty block chain with just a genesis block.
    * Assume genesis block is a valid block
    */
   public BlockChain(Block genesisBlock) {
      // IMPLEMENT THIS
	    UTXOPool uPool = new UTXOPool();      
	    Transaction coinbase = genesisBlock.getCoinbase();      
	    UTXO utxoCoinbase = new UTXO(coinbase.getHash(), 0);      
	    uPool.addUTXO(utxoCoinbase, coinbase.getOutput(0));      
	    BlockNode genesis = new BlockNode(genesisBlock, null, uPool);      
	    heads = new ArrayList<BlockNode>();      
	    heads.add(genesis);      
	    H = new HashMap<ByteArrayWrapper, BlockNode>();      
	    H.put(new ByteArrayWrapper(genesisBlock.getHash()), genesis);      
	    height = 1;      
	    maxHeightBlock = genesis;      
	    txPool = new TransactionPool(); 
   }

   /* Get the maximum height block
    */
   public Block getMaxHeightBlock() {
      // IMPLEMENT THIS
	   Block b = maxHeightBlock.b;
	   return b;
   }
   
   /* Get the UTXOPool for mining a new block on top of 
    * max height block
    */
   public UTXOPool getMaxHeightUTXOPool() {
      // IMPLEMENT THIS
	   return maxHeightBlock.uPool;
   }
   
   /* Get the transaction pool to mine a new block
    */
   public TransactionPool getTransactionPool() {
      // IMPLEMENT THIS
	   return txPool;
   }

   /* Add a block to block chain if it is valid.
    * For validity, all transactions should be valid
    * and block should be at height > (maxHeight - CUT_OFF_AGE).
    * For example, you can try creating a new block over genesis block 
    * (block height 2) if blockChain height is <= CUT_OFF_AGE + 1. 
    * As soon as height > CUT_OFF_AGE + 1, you cannot create a new block at height 2.
    * Return true of block is successfully added
    */
   public boolean addBlock(Block b) {
       // IMPLEMENT THIS
	   if(b.getTransactions().size() == 0) {
		   return true;
	   }
	   
	   byte[] parentHash = b.getPrevBlockHash();
	   if(parentHash == null) {
		   return false;
	   }
	   
	   BlockNode parentBlock = this.H.get(parentHash);
	   
	   if(parentBlock == null) {
		  //b = null;
		  return true;
	   }
	   
	   ArrayList<Transaction> arrayTx = b.getTransactions();
	   
	   int currentHeight = height + 1;
	   if(currentHeight < height - CUT_OFF_AGE) {
		   return false;
	   }
	   
	   //System.out.println(parentBlock.height);
	   
	   TxHandler txHandler = new TxHandler(parentBlock.getUTXOPoolCopy());
	   Transaction[] possibleTxs = new Transaction[arrayTx.size()];
	   possibleTxs = arrayTx.toArray(possibleTxs);
	   
	   for(int i = 0; i < arrayTx.size(); i++) {
		   possibleTxs[i].finalize();
	   }
	   
	   Transaction[] validTransactions = txHandler.handleTxs(possibleTxs);
	   
	   System.out.println("Length of validTransactions: " + validTransactions.length);
	   System.out.println("Length of possibleTxs: " + possibleTxs.length);
	   
	   UTXOPool cUpool = txHandler.getUTXOPool();
	   
	   if(validTransactions.length < possibleTxs.length) {
		   return false;
	   }
	   
	   cUpool.addUTXO(new UTXO(b.getCoinbase().getHash(), 0), b.getCoinbase().getOutput(0));
	   
	   BlockNode newBlockNode = new BlockNode(b, parentBlock, cUpool);
	   H.put(new ByteArrayWrapper(b.getHash()), newBlockNode);
	   heads.add(newBlockNode);
	   
	   if(currentHeight > height) {
		   height++;
		   maxHeightBlock = newBlockNode;
	   }
	   return true;
   }

   /* Add a transaction in transaction pool
    */
   public void addTransaction(Transaction tx) {
      // IMPLEMENT THIS
	   txPool.addTransaction(tx);
   }
}