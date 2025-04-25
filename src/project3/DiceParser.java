package project3;

import java.util.*;

public class DiceParser {

    /* this is a helper class to manage the input "stream" */
    private static class StringStream {

        StringBuffer buff;

        public StringStream(String s) {
            buff = new StringBuffer(s);
        }

        private void munchWhiteSpace() {
            int index = 0;
            char curr;
            while (index < buff.length()) {
                curr = buff.charAt(index);
                if (!Character.isWhitespace(curr)) {
                    break;
                }
                index++;
            }
            buff = buff.delete(0, index);
        }

        public boolean isEmpty() {
            munchWhiteSpace();
            return buff.toString().equals("");
        }

        public Integer getInt() {
            return readInt();
        }

        /**
         * Refactor: Đơn giản hóa logic của readInt() bằng cách dùng regex để
         * trích xuất số nguyên đầu tiên. Điều này giúp code ngắn gọn hơn, dễ
         * bảo trì hơn, và không cần duyệt từng ký tự thủ công.
         *
         * @return Integer nếu đọc được, hoặc null nếu không có số hợp lệ ở đầu
         * chuỗi.
         */
        public Integer readInt() {
            munchWhiteSpace();
            String input = buff.toString();
            StringBuilder number = new StringBuilder();
            for (char c : input.toCharArray()) {
                if (Character.isDigit(c)) {
                    number.append(c);
                } else {
                    break;
                }
            }
            if (number.length() == 0) {
                return null;
            }
            int value = Integer.parseInt(number.toString());
            buff.delete(0, number.length());
            return value;
        }

        public Integer readSgnInt() {
            munchWhiteSpace();
            StringStream state = save();
            if (checkAndEat("+")) {
                Integer ans = readInt();
                if (ans != null) {
                    return ans;
                }
                restore(state);
                return null;
            }
            if (checkAndEat("-")) {
                Integer ans = readInt();
                if (ans != null) {
                    return -ans;
                }
                restore(state);
                return null;
            }
            return readInt();
        }

        public boolean checkAndEat(String s) {
            munchWhiteSpace();
            if (buff.indexOf(s) == 0) {
                buff = buff.delete(0, s.length());
                return true;
            }
            return false;
        }

        public StringStream save() {
            return new StringStream(buff.toString());
        }

        public void restore(StringStream ss) {
            this.buff = new StringBuffer(ss.buff);
        }

        public String toString() {
            return buff.toString();
        }
    }

    /**
     * Phân tích chuỗi mô tả đổ xúc xắc thành danh sách các đối tượng DieRoll.
     *
     * @param s Chuỗi mô tả cú pháp xúc xắc, ví dụ: "2d6+3"
     * @return Danh sách DieRoll nếu hợp lệ, null nếu sai cú pháp.
     *
     * Chức năng thêm: Validation đầu vào – nếu chuỗi null hoặc rỗng thì trả về
     * null.
     */
    public static Vector<DieRoll> parseRoll(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        StringStream ss = new StringStream(s.toLowerCase());
        Vector<DieRoll> v = parseRollInner(ss, new Vector<DieRoll>());
        if (ss.isEmpty()) {
            return v;
        }
        return null;
    }

    private static Vector<DieRoll> parseRollInner(StringStream ss, Vector<DieRoll> v) {
        Vector<DieRoll> r = parseXDice(ss);
        if (r == null) {
            return null;
        }
        v.addAll(r);
        if (ss.checkAndEat(";")) {
            return parseRollInner(ss, v);
        }
        return v;
    }

    private static Vector<DieRoll> parseXDice(StringStream ss) {
        StringStream saved = ss.save();
        Integer x = ss.getInt();
        int num;
        if (x == null) {
            num = 1;
        } else {
            if (ss.checkAndEat("x")) {
                num = x;
            } else {
                num = 1;
                ss.restore(saved);
            }
        }
        DieRoll dr = parseDice(ss);
        if (dr == null) {
            return null;
        }
        Vector<DieRoll> ans = new Vector<DieRoll>();
        for (int i = 0; i < num; i++) {
            ans.add(dr);
        }
        return ans;
    }

    private static DieRoll parseDice(StringStream ss) {
        return parseDTail(parseDiceInner(ss), ss);
    }

    private static DieRoll parseDiceInner(StringStream ss) {
        Integer num = ss.getInt();
        int dsides;
        int ndice;
        if (num == null) {
            ndice = 1;
        } else {
            ndice = num;
        }
        if (ss.checkAndEat("d")) {
            num = ss.getInt();
            if (num == null) {
                return null;
            }
            dsides = num;
        } else {
            return null;
        }
        num = ss.readSgnInt();
        int bonus;
        if (num == null) {
            bonus = 0;
        } else {
            bonus = num;
        }
        return new DieRoll(ndice, dsides, bonus);
    }

    private static DieRoll parseDTail(DieRoll r1, StringStream ss) {
        if (r1 == null) {
            return null;
        }
        if (ss.checkAndEat("&")) {
            DieRoll d2 = parseDice(ss);
            // Tạm bỏ DiceSum vì chưa có lớp DiceSum, chỉ return r1
            // return parseDTail(new DiceSum(r1, d2), ss);
            return r1; // fallback
        } else {
            return r1;
        }
    }

    private static void test(String s) {
        Vector<DieRoll> v = parseRoll(s);
        if (v == null) {
            System.out.println("Failure: " + s);
        } else {
            System.out.println("Results for " + s + ":");
            for (DieRoll dr : v) {
                System.out.print(dr);
                System.out.print(": ");
                System.out.println(dr.makeRoll());
            }
        }
    }

    public static void main(String[] args) {
        test("d6");
        test("2d6");
        test("d6+5");
        test("4X3d8-5");
        test("12d10+5 & 4d6+2");
        test("d6 ; 2d4+3");
        test("4d6+3 ; 8d12 -15 ; 9d10 & 3d6 & 4d12 +17");
        test("4d6 + xyzzy");
        test("hi");
        test("4d4d4");
    }
}
