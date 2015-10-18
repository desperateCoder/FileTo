package main.java.de.c4.model.messages;

public class ContactDto {
	public String name;
	public String ip;
	public EOnlineState state;
	
	
	public ContactDto() {
	}
	
	public ContactDto(String name) {
		super();
		this.name = name;
	}
	public ContactDto(String name, EOnlineState state) {
		super();
		this.name = name;
		this.state = state;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContactDto other = (ContactDto) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		return true;
	}
	
	
	
}
