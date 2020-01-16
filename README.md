# Tomasulo-s-algorithm
## 1.本程式為實作Tomasulo-s-algorithm演算法
* 使用語言 Java(JRE JavaSE-12)
* 開發及編譯工具 Eclipse
* 輸入方式 
 <br/>  1.允許使用者自行輸入ADD(加)、SUB(減)、MUL(乘)、DIV(除)運作之cycle數
 <br/>  2.目前支援ADD、SUB、MUL、DIV等四項基礎運算，其他運算後續補充之
 <br/>  3.允許使用者由原始碼端自行編輯Instruction數量及上述四項運算之運作
 <br/>  4.完成上述初始狀態輸入後，由Eclipse IDE介面(或任何可執行Java語言之IDE)按下執行鍵，軟體即自行運作至所有Instruction取得暫存器之值後，自動停止
 <br/>  5.本次執行案例系將ADD設為2cycle、SUB設為3cycle、MUL設為4cycle、DIV設為5cycle
 <br/>  6.本軟體共模擬IQ Table儲存指令、RS_ADD_Table設置三個空間大小、RS_MUL設置二個空間大小、Reg Table儲存暫存器實際數值(系統預設值自動採1~10亂數產生相關資料)、RAT Table、加減法、乘除法 Buffer暫存器各一個，將在下節實際執行結果畫面呈現。
 
 ## 2.執行結果如下(以紅色方框標註各cycle變化)
 <br/>  1.允許使用者自行輸入ADD(加)、SUB(減)、MUL(乘)、DIV(除)運作之cycle數
![image](1.png "執行結果_1")
 <br/>  2.使用者可由原始碼頁面輸入Instruction指令
![image](3.png "輸入指令畫面_")
 <br/>  3.初始狀態預覽
![image](2.png "初始狀態_")
 <br/>  4.第1個cycle
![image](4.png "第一個cycle")
 <br/>  5.第2個cycle
![image](5.png "第二個cycle")
 <br/>  6.第3個cycle
![image](6.png "第三個cycle")
 <br/>  7.第4個cycle
![image](7.png "第四個cycle")
 <br/>  8.第5個cycle
![image](8.png "第五個cycle")
 <br/>  9.第6個cycle
![image](9.png "第六個cycle")
 <br/>  10.第7個cycle
![image](10.png "第七個cycle")
 <br/>  11.第8個cycle
![image](11.png "第八個cycle")
 <br/>  12.第9個cycle
![image](12.png "第九個cycle")
 <br/>  13.第10個cycle
![image](13.png "第十個cycle")
 <br/>  14.第11個cycle
![image](14.png "第十一個cycle")
<br/>  15.第12個cycle
![image](15.png "第十二個cycle")
<br/>  16.第13個cycle，以本範例，系統自動偵測無其他任何指令等待運算，自動結束運算
![image](16.png "第十三個cycle")

## 3.程式說明
 * 程式5~104行:為本次運算使用之類別，含:
  <br/>  1. Instruction:設定指令數量及指令內容
  <br/>  2. RSAdd:設定加、減運算之RS Table欄位，預設三個空間
  <br/>  3. RSMul:設定乘、除運算之RS Table欄位，預設兩個空間
  <br/>  4. RS_ld:設定讀取、寫入運算之RS Table欄位，本次尚未使用
  <br/>  5. RAT:設定RAT Table欄位資料
  <br/>  6. Dispatch:設定如Dispatch時，Buffer資料內容同步
  <br/>  7. REGS:設定REG Table欄位資料  
<pre><code>
class Instruction {
	String name, op, fsu, fj, fk;
	int count;

	Instruction() {
		this.name = "";
		this.op = "";
		this.fsu = "";
		this.fj = "";
		this.fk = "";
		this.count = 0;
	}

	Instruction(String na, String o, String fs, String j, String k, int co) {
		name = na;
		op = o;
		fsu = fs;
		fj = j;
		fk = k;
		count = co;
	}
}

class RSAdd {
	String ID, BUSY, OP, Qj, Qk, DISP, inst, Vj, Vk;
	int candidate;

	RSAdd() {
		this.ID = " ";
		this.BUSY = " ";
		this.OP = " ";
		this.Vj = " ";
		this.Vk = " ";
		this.Qj = " ";
		this.Qk = " ";
		this.DISP = " ";
		this.inst = " ";
		this.candidate = -1;
	}
}

class RSMul {
	String ID, BUSY, OP, Qj, Qk, DISP, inst, Vj, Vk;

	RSMul() {
		this.ID = " ";
		this.BUSY = " ";
		this.OP = " ";
		this.Vj = " ";
		this.Vk = " ";
		this.Qj = " ";
		this.Qk = " ";
		this.DISP = " ";
		this.inst = " ";
	}
}

class RS_ld {
	Boolean busy, disp;
	String OP, Qj;
	int Vk;

	RS_ld() {
		this.busy = false;
		this.disp = false;
		this.OP = null;
		this.Qj = null;
	}
}

class RAT {
	String rat, content;

	RAT() {
		this.rat = null;
		this.content = null;

	}
}

class Dispatch {
	String ID, Inst, buffer, state;

	Dispatch() {
		this.ID = " ";
		this.Inst = " ";
		this.buffer = " ";
		this.state = " ";
	}
}

class REGS {
	String reg;
	int content;

	REGS() {
		this.reg = null;

	}
}</code></pre>

 * 程式106~292行:為主程式，說明如下
  <br/>  1. 106~126行:由使用者輸入+、-、x、/指令運算所需cycle數
 <pre><code>
  public class tomasulo_algorithm {

	public static void main(String[] args) {
		int cycle = 1;
		int ADD_cycle;
		int SUB_cycle;
		int MUL_cycle;
		int DIV_cycle;
		Scanner sc = new Scanner(System.in);
		System.out.print("請輸入 ADD cycle數 :  ");
		ADD_cycle = sc.nextInt();
		System.out.println();
		System.out.print("請輸入 SUB cycle數 :  ");
		SUB_cycle = sc.nextInt();
		System.out.println();
		System.out.print("請輸入 MUL cycle數 :  ");
		MUL_cycle = sc.nextInt();
		System.out.println();
		System.out.print("請輸入 DIV cycle數 :  ");
		DIV_cycle = sc.nextInt();
		System.out.println();</code></pre>
