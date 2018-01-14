package com.alkefp.sales.vo;

import java.util.List;

public class ClientSalesDataSetVO {

        private List<Double> data;
        private String label;
        private String bgColor;
        private String borderColor;
		public List<Double> getData() {
			return data;
		}
		public void setData(List<Double> data) {
			this.data = data;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getBgColor() {
			return bgColor;
		}
		public void setBgColor(String bgColor) {
			this.bgColor = bgColor;
		}
		public String getBorderColor() {
			return borderColor;
		}
		public void setBorderColor(String borderColor) {
			this.borderColor = borderColor;
		}
}
