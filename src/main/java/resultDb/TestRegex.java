package resultDb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String str= "Genefunction";
		String regex1 = "/[gG]en[eo]./gm";
		Pattern pattern1 = Pattern.compile(regex1,
				Pattern.CASE_INSENSITIVE);

		Matcher matcher6 = pattern1.matcher(str);

		if (matcher6.find()){
			System.out.println(true);
		}


	}

}
