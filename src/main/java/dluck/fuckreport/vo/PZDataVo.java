package dluck.fuckreport.vo;

public class PZDataVo {
	private String OptionName;
	private String SelectId;
	private String TitleId;
	private String OptionType;

	public PZDataVo(String optionName) {
		OptionName = optionName;
	}

	public String getOptionName() {
		return OptionName;
	}

	public void setOptionName(String optionName) {
		OptionName = optionName;
	}

	public String getSelectId() {
		return SelectId;
	}

	public void setSelectId(String selectId) {
		SelectId = selectId;
	}

	public String getTitleId() {
		return TitleId;
	}

	public void setTitleId(String titleId) {
		TitleId = titleId;
	}

	public String getOptionType() {
		return OptionType;
	}

	public void setOptionType(String optionType) {
		OptionType = optionType;
	}
}
