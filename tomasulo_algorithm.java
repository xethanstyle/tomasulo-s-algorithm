package test;

import java.util.*;

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
}

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
		System.out.println();
		Instruction Inst[] = new Instruction[4];
		Inst[0] = new Instruction("INST1", "MUL", "F3", "F2", "F1", 0);
		Inst[1] = new Instruction("INST2", "SUB", "F0", "F3", "F5", 0);
		Inst[2] = new Instruction("INST3", "ADD", "F10", "F1", "F0", 0);
		Inst[3] = new Instruction("INST4", "DIV", "F6", "F8", "F2", 0);

		TreeSet RegSet = RegSet(Inst);

		// System.out.println(RegSet);

		int MulcyCount[] = new int[1];

		int AddcyCount[] = new int[1];

		System.out.println("\n加法需要 " + ADD_cycle + " 個cycle、減法需要 " + SUB_cycle + " 個cycle、乘法需要 " + MUL_cycle
				+ " 個cycle、除法需要 " + DIV_cycle + " 個cycle !!\n");

		int RegArray[] = RegArray(RegSet);

		REGS RegTable[] = initRegTable(RegArray); // 初始化REG Table

		RAT RATTable[] = initRatTable(RegArray); // 初始化Rat Table

		RSAdd RSAdd[] = initRSAddTable(); // 初始化RsAdd Table

		RSMul RSMul[] = initRSMulTable(); // 初始化RsAdd Table

		Dispatch Dispatch_Table[] = initDispatch(); // 初始化Dispatch Table

		boolean hasnext = true;

		System.out.println("(初始狀態)  Cycel: 0");
		print(RegTable, Inst, RATTable, RSAdd, RSMul, Dispatch_Table);

		do {
			System.out.println("Cycel: " + cycle);
			if ((Dispatch_Table[1].buffer.equals("execute")) && (Dispatch_Table[0].buffer.equals("execute"))) { // 如果buffer有inst在執行的狀況，此狀況為ADD
																												// Buffer及MUL
																												// Buffer同時都為execute狀態
				writeback_1(RSMul, RSAdd, Dispatch_Table, RATTable, RegTable, MulcyCount, cycle, MUL_cycle, // 檢查該 MUL
																											// buffer是否可writeback
						DIV_cycle);
				writeback_2(RSMul, RSAdd, Dispatch_Table, RATTable, RegTable, AddcyCount, cycle, ADD_cycle, // 檢查該ADD
																											// buffer是否可writeback
						SUB_cycle);
				if (Inst[0].op.equals("ADD") || Inst[0].op.equals("SUB")) { // 同時檢查IQ table是否可 issue
					RSAdd RsAdd[] = issue_2(Inst, RSAdd, RATTable, RegTable);
				} else if (Inst[0].op.equals("MUL") || Inst[0].op.equals("DIV")) {
					RSMul RsMul[] = issue_1(Inst, RSMul, RATTable, RegTable);
				}
				cycle = cycle + 1; // write剛執行完後，需下個迴圈才能進入Dispatch
				RSAdd = dispatch_2(RSAdd, Dispatch_Table); // 檢查RS ADD是否可dispatch
				RSMul = dispatch_1(RSMul, Dispatch_Table); // 檢查RS MUL是否可dispatch
				print(RegTable, Inst, RATTable, RSAdd, RSMul, Dispatch_Table);
			}

			else if ((Dispatch_Table[1].buffer.equals("execute")) || (Dispatch_Table[0].buffer.equals("execute"))) { // 如果buffer有inst在執行的狀況，此狀況為ADD
																														// Buffer
																														// "或"
																														// MUL
																														// Buffer為execute狀態
				if ((Dispatch_Table[1].buffer.equals("execute"))) { // Dispatch_Table[1].ID = "MUL" , 如果是MUL
																	// Buffer為execute狀態
					writeback_1(RSMul, RSAdd, Dispatch_Table, RATTable, RegTable, MulcyCount, cycle, MUL_cycle,
							DIV_cycle);
					if ((!(Dispatch_Table[0].state.isBlank()) && (!(Dispatch_Table[0].buffer.equals("execute"))))
							&& (!Dispatch_Table[0].state.isBlank())) { // 同個cycle，有可能RS ADD有inst可以準備excute
						AddcyCount[0] = cycle;
						Dispatch_Table[0].buffer = "execute";
						Dispatch_Table[0].Inst = Dispatch_Table[0].state;
						for (int i = 0; i < RSAdd.length; i++) {
							if (RSAdd[i].ID.equals(Dispatch_Table[0].state)) {
								RSAdd[i].DISP = "Exec" + Integer.toString((cycle - AddcyCount[0] + 1));
								break;
							}
						}
					}
					cycle = cycle + 1;
				}

				else if ((Dispatch_Table[0].buffer.equals("execute"))) { // Dispatch_Table[0].ID = "ADD" , 如果是ADD
																			// Buffer為execute狀態
					writeback_2(RSMul, RSAdd, Dispatch_Table, RATTable, RegTable, AddcyCount, cycle, ADD_cycle,
							SUB_cycle);
					if ((!(Dispatch_Table[1].state.isBlank()) && (!(Dispatch_Table[1].buffer.equals("execute"))))
							&& (!Dispatch_Table[1].state.isBlank())) { // //同個cycle，有可能RS ADD有inst可以準備excute
						MulcyCount[0] = cycle;
						Dispatch_Table[1].buffer = "execute";
						Dispatch_Table[1].Inst = Dispatch_Table[1].state;
						for (int i = 0; i < RSMul.length; i++) {
							if (RSMul[i].ID.equals(Dispatch_Table[1].state)) {
								RSMul[i].DISP = "Exec" + Integer.toString((cycle - MulcyCount[0] + 1));
								break;
							}
						}
					}
					cycle = cycle + 1;
				}

				if (Inst[0].op.equals("ADD") || Inst[0].op.equals("SUB")) { // 同個cycle可再檢查是否有inst可issue
					RSAdd RsAdd[] = issue_2(Inst, RSAdd, RATTable, RegTable);
				} else if (Inst[0].op.equals("MUL") || Inst[0].op.equals("DIV")) {
					RSMul RsMul[] = issue_1(Inst, RSMul, RATTable, RegTable);
				}
				RSAdd = dispatch_2(RSAdd, Dispatch_Table); // 同個cycle可再檢查是否有inst可準備進入dispatch
				RSMul = dispatch_1(RSMul, Dispatch_Table);
				print(RegTable, Inst, RATTable, RSAdd, RSMul, Dispatch_Table);
			}

			else if ((!(Dispatch_Table[0].state.isBlank()) && (!(Dispatch_Table[0].buffer.equals("execute"))))
					|| (!(Dispatch_Table[1].state.isBlank()) && (!(Dispatch_Table[1].buffer.equals("execute"))))) { // ADD及MULTI
																													// Buffer均為閒置狀態
				if ((!(Dispatch_Table[0].state.isBlank()) && (!(Dispatch_Table[0].buffer.equals("execute"))))
						&& (!Dispatch_Table[0].state.isBlank())) {
					AddcyCount[0] = cycle;
					Dispatch_Table[0].buffer = "execute";
					Dispatch_Table[0].Inst = Dispatch_Table[0].state;
				}
				if ((!(Dispatch_Table[1].state.isBlank()) && (!(Dispatch_Table[1].buffer.equals("execute"))))
						&& (!Dispatch_Table[1].state.isBlank())) {
					MulcyCount[0] = cycle;
					Dispatch_Table[1].buffer = "execute";
					Dispatch_Table[1].Inst = Dispatch_Table[1].state;

				}
				cycle = cycle + 1;
				for (int i = 0; i < RSAdd.length; i++) {
					if (RSAdd[i].ID.equals(Dispatch_Table[0].state)) {
						RSAdd[i].DISP = "Exec" + Integer.toString((cycle - AddcyCount[0]));
						break;
					}
				}
				for (int i = 0; i < RSMul.length; i++) {
					if (RSMul[i].ID.equals(Dispatch_Table[1].state)) {
						RSMul[i].DISP = "Exec" + Integer.toString((cycle - MulcyCount[0]));
						break;

					}
				}

				if (Inst[0].op.equals("ADD") || Inst[0].op.equals("SUB")) {
					RSAdd RsAdd[] = issue_2(Inst, RSAdd, RATTable, RegTable);
				} else if (Inst[0].op.equals("MUL") || Inst[0].op.equals("DIV")) {
					RSMul RsMul[] = issue_1(Inst, RSMul, RATTable, RegTable);
				}

				print(RegTable, Inst, RATTable, RSAdd, RSMul, Dispatch_Table);
			}

			else {

				if (Inst[0].op.equals("ADD") || Inst[0].op.equals("SUB")) {
					RSAdd RsAdd[] = issue_2(Inst, RSAdd, RATTable, RegTable);
					cycle = cycle + 1;
				} else if (Inst[0].op.equals("MUL") || Inst[0].op.equals("DIV")) {
					RSMul RsMul[] = issue_1(Inst, RSMul, RATTable, RegTable);
					cycle = cycle + 1;
				}
				RSMul = dispatch_1(RSMul, Dispatch_Table);
				RSAdd = dispatch_2(RSAdd, Dispatch_Table);
				print(RegTable, Inst, RATTable, RSAdd, RSMul, Dispatch_Table);
			}

		} while (hasNext(hasnext, Inst, RSAdd, RSMul) == true);

	}

	// 以下為函式部分

	public static TreeSet RegSet(Instruction inst[]) { // 計算本次運算有那些Reg，存入TreeSet，目的將重複出現之Reg刪除
		TreeSet<String> RegSet = new TreeSet<String>();
		for (int i = 0; i < inst.length; i++) {
			RegSet.add(inst[i].fsu);
			RegSet.add(inst[i].fj);
			RegSet.add(inst[i].fk);
		}
		return RegSet;
	}

	public static int[] RegArray(TreeSet RegSet) { // 將RegSet轉成RegArray，並由小到大完成排序
		Object[] Reg = RegSet.toArray();
		int tmp;
		int RegArr[] = new int[Reg.length];
		for (int i = 0; i < Reg.length; i++) {
			RegArr[i] = Integer.parseInt(Reg[i].toString().split("F")[1]);
		}
		for (int i = RegArr.length - 1; i >= 0; i--) { // 由小到大排序
			for (int j = 0; j < i; j = j + 1) {
				if (RegArr[j] > RegArr[i]) {
					tmp = RegArr[j];
					RegArr[j] = RegArr[i];
					RegArr[i] = tmp;
				}
			}
		}
		return RegArr;
	}

	public static REGS[] initRegTable(int RegArray[]) { // 初始化Reg Table
		REGS RegTable[] = new REGS[RegArray.length];

		for (int i = 0; i < RegTable.length; i++) {
			RegTable[i] = new REGS();
			RegTable[i].reg = "F" + Integer.toString(RegArray[i]);

			RegTable[i].content = (int) (Math.random() * 10) + 1; // 亂數產生RegTable內的值，範圍1~10

		}
		return RegTable;
	}

	public static RAT[] initRatTable(int RegArray[]) { // 初始化RAT Table
		RAT RatTable[] = new RAT[RegArray.length];

		for (int i = 0; i < RatTable.length; i++) {
			RatTable[i] = new RAT();
			RatTable[i].rat = "F" + Integer.toString(RegArray[i]);
			RatTable[i].content = " ";
		}
		return RatTable;
	}

	public static RSAdd[] initRSAddTable() { // 初始化RS Add
		RSAdd RsA_Table[] = new RSAdd[3];
		for (int i = 0; i < RsA_Table.length; i++) {
			RsA_Table[i] = new RSAdd();
			RsA_Table[i].ID = "RS" + Integer.toString(i + 1);
			RsA_Table[i].OP = " ";
			RsA_Table[i].Qj = " ";
			RsA_Table[i].Qk = " ";
		}
		return RsA_Table;
	}

	public static RSMul[] initRSMulTable() { // 初始化RS Mul
		RSMul RsM_Table[] = new RSMul[2];
		for (int i = 0; i < RsM_Table.length; i++) {
			RsM_Table[i] = new RSMul();

			RsM_Table[i].OP = " ";
			RsM_Table[i].Qj = " ";
			RsM_Table[i].Qk = " ";
		}
		RsM_Table[0].ID = "RS4";
		RsM_Table[1].ID = "RS5";
		return RsM_Table;
	}

	public static Dispatch[] initDispatch() { // 初始化Dispatch
		Dispatch Dispatch_Table[] = new Dispatch[2];
		for (int i = 0; i < Dispatch_Table.length; i++)
			Dispatch_Table[i] = new Dispatch();
		Dispatch_Table[0].ID = "ADD";
		Dispatch_Table[1].ID = "MULTI";
		return Dispatch_Table;
	}

	public static RSMul[] issue_1(Instruction inst[], RSMul RsM[], RAT RAT_Table[], REGS REG_Table[]) { // 計算一個cycle後,RSMul
																										// Table狀態
		int index = -1;
		for (int i = 0; i < RsM.length; i++) {
			if (RsM[i].BUSY == " " && inst.length != 0) { // 判斷RS空間是否足夠
				RsM[i].BUSY = inst[0].fsu;
				RsM[i].OP = inst[0].op;
				RsM[i].inst = inst[0].name;
				index = i;
				break;
			}
		}
		if (index != -1) {
			for (int j = 0; j < REG_Table.length; j++) {
				if (RAT_Table[j].rat.equals(inst[0].fj) && (!RAT_Table[j].content.equals(" "))) {
					RsM[index].Qj = RAT_Table[j].content;
					break;
				} else if (RAT_Table[j].rat.equals(inst[0].fj) && RAT_Table[j].content.equals(" ")) {
					RsM[index].Vj = Integer.toString(REG_Table[j].content);
					break;
				}
			}

			for (int j = 0; j < REG_Table.length; j++) {
				if (RAT_Table[j].rat.equals(inst[0].fk) && (!RAT_Table[j].content.equals(" "))) {
					RsM[index].Qk = RAT_Table[j].content;
					break;
				}

				else if (RAT_Table[j].rat.equals(inst[0].fk) && RAT_Table[j].content.equals(" ")) {
					RsM[index].Vk = Integer.toString(REG_Table[j].content);
					break;
				}
			}

			for (int i = 0; i < RAT_Table.length; i++) { // 更新RAT Table
				if (RAT_Table[i].rat.equals(RsM[index].BUSY)) {
					RAT_Table[i].content = RsM[index].ID;
				}
			}

			for (int i = 0; i < RsM.length; i++) { // 如果Instruction有被Issue，重新整理IQ Table
				if (RsM[i].inst.equals(inst[0].name)) {
					System.out.println("Issue: " + inst[0].name + "  " + inst[0].op + "  " + inst[0].fsu + "  "
							+ inst[0].fj + " " + inst[0].fk);
					for (int j = 0; j < (inst.length - 1); j++) {
						inst[j] = inst[j + 1];
					}
					inst[inst.length - 1] = new Instruction();
					break;
				}
			}

		}

		return RsM;
	}

	public static RSAdd[] issue_2(Instruction inst[], RSAdd RsA[], RAT RAT_Table[], REGS REG_Table[]) { // 計算一個cycle後,RSAdd
																										// Table狀態
		int index = -1;
		for (int i = 0; i < RsA.length; i++) {
			if (RsA[i].BUSY == " " && inst.length != 0) { // 判斷RS空間是否足夠
				RsA[i].BUSY = inst[0].fsu;
				RsA[i].OP = inst[0].op;
				RsA[i].inst = inst[0].name;
				index = i;
				break;
			}
		}
		if (index != -1) {
			for (int j = 0; j < REG_Table.length; j++) {
				if (RAT_Table[j].rat.equals(inst[0].fj) && (!RAT_Table[j].content.equals(" "))) {
					RsA[index].Qj = RAT_Table[j].content;
					break;
				} else if (RAT_Table[j].rat.equals(inst[0].fj) && RAT_Table[j].content.equals(" ")) {
					RsA[index].Vj = Integer.toString(REG_Table[j].content);
					break;
				}
			}

			for (int j = 0; j < REG_Table.length; j++) {
				if (RAT_Table[j].rat.equals(inst[0].fk) && (!RAT_Table[j].content.equals(" "))) {
					RsA[index].Qk = RAT_Table[j].content;
					break;
				}

				else if (RAT_Table[j].rat.equals(inst[0].fk) && RAT_Table[j].content.equals(" ")) {
					RsA[index].Vk = Integer.toString(REG_Table[j].content);
					break;
				}
			}

			for (int i = 0; i < RAT_Table.length; i++) { // 更新RAT Table
				if (RAT_Table[i].rat.equals(RsA[index].BUSY)) {
					RAT_Table[i].content = RsA[index].ID;
				}
			}

			for (int i = 0; i < RsA.length; i++) { // 如果Instruction有被Issue，重新整理IQ Table
				if (RsA[i].inst.equals(inst[0].name)) {
					System.out.println("Issue: " + inst[0].name + "  " + inst[0].op + "  " + inst[0].fsu + "  "
							+ inst[0].fj + " " + inst[0].fk);
					for (int j = 0; j < (inst.length - 1); j++) {
						inst[j] = inst[j + 1];
					}
					inst[inst.length - 1] = new Instruction();
					break;
				}
			}
		}
		return RsA;
	}

	public static RSMul[] dispatch_1(RSMul RsM[], Dispatch Dispatch_Table[]) { // 計算哪一個RS已經ready，可以進入執行
		int dispatch_index = -1;
		int count = 0;
		for (int i = 0; i < RsM.length; i++) {
			if (!(RsM[i].Vj.isBlank()) && !(RsM[i].Vk.isBlank()) && (RsM[i].Qj.isBlank() && RsM[i].Qk.isBlank())
					&& RsM[i].DISP.isBlank() && !(RsM[i].DISP.equals("Execute"))) {
				count++;
				dispatch_index = i;
			}
		}
		if (count == 1 && Dispatch_Table[1].buffer.isBlank()) { // 檢查RS有幾個inst已經ready,且execute
																// buffer是空的，本項為inst僅有一個ready的狀況
			RsM[dispatch_index].DISP = "ready";
			Dispatch_Table[1].state = RsM[dispatch_index].ID;
		} else if (count > 1 && Dispatch_Table[1].buffer.isBlank()) { // 檢查RS有幾個inst已經ready,且execute
																		// buffer是空的，本項為inst僅有二個以上ready的狀況，採用random方式dispatch
			dispatch_index = (int) (Math.random() * 2);
			System.out.println(dispatch_index);
			RsM[dispatch_index].DISP = "ready";
			Dispatch_Table[1].state = RsM[dispatch_index].ID;
		}
		return RsM;
	}

	public static RSAdd[] dispatch_2(RSAdd RsA[], Dispatch Dispatch_Table[]) { // 計算哪一個RS已經ready，可以進入執行
		int dispatch_index = -1;
		int count = 0;
		for (int i = 0; i < RsA.length; i++) {
			if (!(RsA[i].Vj.isBlank()) && !(RsA[i].Vk.isBlank()) && (RsA[i].Qj.isBlank() && RsA[i].Qk.isBlank())
					&& RsA[i].DISP.isBlank() && !(RsA[i].DISP.equals("Execute"))) {
				count++;
				dispatch_index = i;
				RsA[i].candidate = i;
			}
		}
		if (count == 1 && Dispatch_Table[0].buffer.isBlank()) { // 檢查RS有幾個inst已經ready,且execute
																// buffer是空的，本項為inst僅有一個ready的狀況
			RsA[dispatch_index].DISP = "ready";
			Dispatch_Table[0].state = RsA[dispatch_index].ID;
		} else if (count == 2 && Dispatch_Table[0].buffer.isBlank()) { // 檢查RS有幾個inst已經ready,且execute
																		// buffer是空的，本項為inst有二個ready的狀況，採用random方式dispatch
			LinkedList list = new LinkedList();
			for (int i = 0; i < RsA.length; i++) {
				if (RsA[i].candidate != -1) {
					list.add(RsA[i].candidate);
				}
			}
			dispatch_index = (int) list.get((int) (Math.random() * 2));
			RsA[dispatch_index].DISP = "ready";
			Dispatch_Table[0].state = RsA[dispatch_index].ID;
		} else if (count == 3 && Dispatch_Table[0].buffer.isBlank()) { // 檢查RS有幾個inst已經ready,且execute
																		// buffer是空的，本項為inst三個均ready的狀況，採用random方式dispatch
			dispatch_index = (int) (Math.random() * 3);
			RsA[dispatch_index].DISP = "ready";
			Dispatch_Table[0].state = RsA[dispatch_index].ID;
		}
		for (int i = 0; i < RsA.length; i++) { // 重置candidate
			RsA[i].candidate = -1;
		}
		return RsA;
	}

	public static void writeback_1(RSMul RsM[], RSAdd RsAdd[], Dispatch Dispatch_Table[], RAT RATTable[],
			REGS RegTable[], int MulcyCount[], int cycle, int MUL_cycle, int DIV_cycle) {
		int index = -1;
		String result = "";
		String Reg = "";
		for (int i = 0; i < RsM.length; i++) {
			if (RsM[i].DISP.contains("Exec")) {
				index = i;
			}
		}
		RsM[index].DISP = "Exec" + Integer.toString(cycle - MulcyCount[0] + 1);
		if (RsM[index].OP.equals("MUL")) { // 乘法的部分

			if ((cycle - MulcyCount[0]) == MUL_cycle) {
				result = Integer.toString((Integer.parseInt(RsM[index].Vj) * Integer.parseInt(RsM[index].Vk)));// 計算得到的值
				Reg = RsM[index].ID;
				RsM[index] = new RSMul();
				if (index == 0) {
					RsM[index].ID = "RS4";
				} else {
					RsM[index].ID = "RS5";
				}
				MulcyCount = new int[1];
				Dispatch_Table[1].buffer = " ";
				Dispatch_Table[1].Inst = " ";
				Dispatch_Table[1].state = " ";

				for (int j = 0; j < RATTable.length; j++) {
					if (RATTable[j].content.equals(Reg)) { // 如果RAT Table有相同的暫存器名稱，才修改RegTable，否則不修改
						RegTable[j].content = Integer.parseInt(result); // 更新Reg Table裡對應的值
						RATTable[j].content = " "; // 消除RAT Table之前對應的值
						break;
					}
				}
				for (int k = 0; k < RsAdd.length; k++) { // 檢查其他RS_Add Table是否有參照，並更新
					if (RsAdd[k].Qj.equals(Reg)) {
						RsAdd[k].Vj = result;
						RsAdd[k].Qj = " ";
					} else if (RsAdd[k].Qk.equals(Reg)) {
						RsAdd[k].Vk = result;
						RsAdd[k].Qk = " ";
					}
				}
				for (int k = 0; k < RsM.length; k++) { // 檢查其他RS_MUL Table是否有參照，並更新
					if (RsM[k].Qj.equals(Reg)) {
						RsM[k].Vj = result;
						RsM[k].Qj = " ";
					} else if (RsM[k].Qk.equals(Reg)) {
						RsM[k].Vk = result;
						RsM[k].Qk = " ";
					}
				}
			}
		} else if (RsM[index].OP.equals("DIV")) { // 除法的部分
			if ((cycle - MulcyCount[0]) == DIV_cycle) {
				result = Integer.toString((Integer.parseInt(RsM[index].Vj) / Integer.parseInt(RsM[index].Vk)));// 計算得到的值
				Reg = RsM[index].ID;
				System.out.println("597");
				RsM[index] = new RSMul();
				System.out.println("598");
				if (index == 0) {
					RsM[index].ID = "RS4";
				} else {
					RsM[index].ID = "RS5";
				}
				MulcyCount = new int[1];
				System.out.println("604");
				Dispatch_Table[1].buffer = " ";
				Dispatch_Table[1].Inst = " ";
				Dispatch_Table[1].state = " ";
				System.out.println("609");

				for (int j = 0; j < RATTable.length; j++) {
					if (RATTable[j].content.equals(Reg)) { // 如果RAT Table有相同的暫存器名稱，才修改RegTable，否則不修改
						RegTable[j].content = Integer.parseInt(result); // 更新Reg Table裡對應的值
						RATTable[j].content = " "; // 消除RAT Table之前對應的值
						break;
					}
				}
				for (int k = 0; k < RsAdd.length; k++) { // 檢查其他RS_Add Table是否有參照，並更新
					if (RsAdd[k].Qj.equals(Reg)) {
						RsAdd[k].Vj = result;
						RsAdd[k].Qj = " ";
					} else if (RsAdd[k].Qk.equals(Reg)) {
						RsAdd[k].Vk = result;
						RsAdd[k].Qk = " ";
					}
				}
				for (int k = 0; k < RsM.length; k++) { // 檢查其他RS_MUL Table是否有參照，並更新
					if (RsM[k].Qj.equals(Reg)) {
						RsM[k].Vj = result;
						RsM[k].Qj = " ";
					} else if (RsM[k].Qk.equals(Reg)) {
						RsM[k].Vk = result;
						RsM[k].Qk = " ";
					}
				}
			}
		}
	}

	public static void writeback_2(RSMul RsM[], RSAdd RsA[], Dispatch Dispatch_Table[], RAT RATTable[], REGS RegTable[],
			int AddcyCount[], int cycle, int ADD_cycle, int SUB_cycle) {
		int index = -1;
		String result = "";
		String Reg = "";
		for (int i = 0; i < RsA.length; i++) {
			if (RsA[i].DISP.contains("Exec")) {
				index = i;
			}
		}
		RsA[index].DISP = "Exec" + Integer.toString(cycle - AddcyCount[0] + 1);
		if (RsA[index].OP.equals("ADD")) { // 加法的部分
			if ((cycle - AddcyCount[0]) == ADD_cycle) {
				result = Integer.toString((Integer.parseInt(RsA[index].Vj) + Integer.parseInt(RsA[index].Vk)));// 計算得到的值
				Reg = RsA[index].ID;
				RsA[index] = new RSAdd();
				if (index == 0) {
					RsA[index].ID = "RS1";
				} else if (index == 1) {
					RsA[index].ID = "RS2";
				} else
					RsA[index].ID = "RS3";
				AddcyCount = new int[1];
				Dispatch_Table[0].buffer = " ";
				Dispatch_Table[0].Inst = " ";
				Dispatch_Table[0].state = " ";

				for (int j = 0; j < RATTable.length; j++) {
					if (RATTable[j].content.equals(Reg)) { // 如果RAT Table有相同的暫存器名稱，才修改RegTable，否則不修改
						RegTable[j].content = Integer.parseInt(result); // 更新Reg Table裡對應的值
						RATTable[j].content = " "; // 消除RAT Table之前對應的值
						break;
					}
				}
				for (int k = 0; k < RsA.length; k++) { // 檢查其他RS_Add Table是否有參照，並更新
					if (RsA[k].Qj.equals(Reg)) {
						RsA[k].Vj = result;
						RsA[k].Qj = " ";
					} else if (RsA[k].Qk.equals(Reg)) {
						RsA[k].Vk = result;
						RsA[k].Qk = " ";
					}
				}
				for (int k = 0; k < RsM.length; k++) { // 檢查其他RS_MUL Table是否有參照，並更新
					if (RsM[k].Qj.equals(Reg)) {
						RsM[k].Vj = result;
						RsM[k].Qj = " ";
					} else if (RsM[k].Qk.equals(Reg)) {
						RsM[k].Vk = result;
						RsM[k].Qk = " ";
					}
				}
			}
		} else if (RsA[index].OP.equals("SUB")) { // 減法的部分
			if ((cycle - AddcyCount[0]) == SUB_cycle) {
				result = Integer.toString((Integer.parseInt(RsA[index].Vj) - Integer.parseInt(RsA[index].Vk)));// 計算得到的值
				Reg = RsA[index].ID;
				RsA[index] = new RSAdd();
				if (index == 0) {
					RsA[index].ID = "RS1";
				} else if (index == 1) {
					RsA[index].ID = "RS2";
				} else
					RsA[index].ID = "RS3";
				AddcyCount = new int[1];
				Dispatch_Table[0].buffer = " ";
				Dispatch_Table[0].Inst = " ";
				Dispatch_Table[0].state = " ";

				for (int j = 0; j < RATTable.length; j++) {
					if (RATTable[j].content.equals(Reg)) { // 如果RAT Table有相同的暫存器名稱，才修改RegTable，否則不修改
						RegTable[j].content = Integer.parseInt(result); // 更新Reg Table裡對應的值
						RATTable[j].content = " "; // 消除RAT Table之前對應的值
						break;
					}
				}
				for (int k = 0; k < RsA.length; k++) { // 檢查其他RS_Add Table是否有參照，並更新
					if (RsA[k].Qj.equals(Reg)) {
						RsA[k].Vj = result;
						RsA[k].Qj = " ";
					} else if (RsA[k].Qk.equals(Reg)) {
						RsA[k].Vk = result;
						RsA[k].Qk = " ";
					}
				}
				for (int k = 0; k < RsM.length; k++) { // 檢查其他RS_MUL Table是否有參照，並更新
					if (RsM[k].Qj.equals(Reg)) {
						RsM[k].Vj = result;
						RsM[k].Qj = " ";
					} else if (RsM[k].Qk.equals(Reg)) {
						RsM[k].Vk = result;
						RsM[k].Qk = " ";
					}
				}
			}
		}
	}

	public static boolean hasNext(boolean hasnext, Instruction Inst[], RSAdd RsA[], RSMul RsM[]) { // 當inst,RsA,RsM等Table還有值時，回傳true
		boolean Inst_hasnext = true;
		boolean RsA_hasnext = true;
		boolean RsM_hasnext = true;
		if (Inst[0].name.isBlank()) {
			Inst_hasnext = false;
		}

		for (int i = 0; i < RsA.length; i++) {
			if (!(RsA[i].BUSY.isBlank())) {
				RsA_hasnext = true;
				break;
			} else
				RsA_hasnext = false;
		}

		for (int i = 0; i < RsM.length; i++) {
			if (!(RsM[i].BUSY.isBlank())) {
				RsM_hasnext = true;
				break;
			} else
				RsM_hasnext = false;
		}

		hasnext = (Inst_hasnext || RsA_hasnext || RsM_hasnext);
		return hasnext;

	}

	public static void print(REGS RegTable[], Instruction Instruction[], RAT RatTable[], RSAdd RSAddTable[],
			RSMul RSMulTable[], Dispatch Dispatch_Table[]) { // 列印
		System.out.println("\n   IQ Table :\n");
		for (int i = Instruction.length - 1; i >= 0; i--) {
			System.out.print("\t| " + Instruction[i].name + "\t|  " + Instruction[i].op + "\t" + Instruction[i].fsu
					+ "   " + Instruction[i].fj + "  " + Instruction[i].fk + "\t|");
			System.out.println();
			System.out.println("\t----------------------------------");
		}

		System.out.println("\n   RS_Add Table :\t\t\t\t\t\t\t\tRS_MUL Table :\n");
		System.out.print("\t| " + " ID" + "\t|" + " BUSY" + "\t|  " + "OP" + "\t|  " + "Vj" + "\t|  " + "Vk" + "\t|  "
				+ "Qj" + "\t|  " + "Qk" + "\t|  " + "DISP" + "\t|" + "\t| " + " ID" + "\t|" + " BUSY" + "\t|  " + "OP"
				+ "\t|  " + "Vj" + "\t|  " + "Vk" + "\t|  " + "Qj" + "\t|  " + "Qk" + "\t|  " + "DISP" + "\t|\n");
		System.out.println(
				"\t------------------------------------------------------------------\t-----------------------------------------------------------------");
		for (int i = 0; i < RSAddTable.length; i++) {
			System.out.print("\t|  " + RSAddTable[i].ID + "\t|  " + RSAddTable[i].BUSY + "\t|  " + RSAddTable[i].OP
					+ "\t|   " + RSAddTable[i].Vj + "\t|   " + RSAddTable[i].Vk + "\t|  " + RSAddTable[i].Qj + "\t|  "
					+ RSAddTable[i].Qk + "\t| " + RSAddTable[i].DISP + "\t|");
			if (i == 0) {
				System.out.println();
			}
			if (i >= 1) {
				System.out.print("\t|  " + RSMulTable[i - 1].ID + "\t|  " + RSMulTable[i - 1].BUSY + "\t|  "
						+ RSMulTable[i - 1].OP + "\t|   " + RSMulTable[i - 1].Vj + "\t|   " + RSMulTable[i - 1].Vk
						+ "\t|  " + RSMulTable[i - 1].Qj + "\t|  " + RSMulTable[i - 1].Qk + "\t| "
						+ RSMulTable[i - 1].DISP + "\t|\n");
			}
			System.out.println(
					"\t-----------------------------------------------------------------\t-----------------------------------------------------------------");
		}

		System.out.print("\n   Reg Table: \t\t\t RAT Table:\n\n");
		for (int i = 0; i < RegTable.length; i++) {
			System.out.print("\t|  " + RegTable[i].reg + "\t|");
			System.out.print("   " + RegTable[i].content + "\t|");
			System.out.print("\t\t |  " + RatTable[i].rat + "\t|");
			System.out.print("  " + RatTable[i].content + "\t| \n");
		}
		System.out.println();

		System.out.print("\n   Dispatch Table:\n\n");

		System.out
				.print("  \t|" + "\t" + Dispatch_Table[0].ID + "\t  |\t\t|" + "\t" + Dispatch_Table[1].ID + "\t   |\n");
		System.out.println("\t------------------\t\t ------------------");
		System.out.print(
				"  \t|" + "\t" + Dispatch_Table[0].Inst + "\t  |\t\t|" + "\t" + Dispatch_Table[1].Inst + "\t   |\n");
		System.out.println("\t------------------\t\t ------------------");
		System.out.print("  \t|" + "\t" + Dispatch_Table[0].buffer + "\t  |\t\t|" + "\t" + Dispatch_Table[1].buffer
				+ "\t   |\n");
		System.out.println();
		System.out.println(
				"==========================================================================================\n");
	}
}
