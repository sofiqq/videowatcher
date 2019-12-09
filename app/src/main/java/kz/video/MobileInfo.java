package kz.video;

import java.io.Serializable;
import java.util.ArrayList;

public class MobileInfo implements Serializable {

    int error_id;
    int price;
    ArrayList<String> tech_spec;
    ArrayList<String> add_info;

    public MobileInfo(int error_id, int price, ArrayList<String> tech_spec, ArrayList<String> add_info) {
        this.error_id = error_id;
        this.price = price;
        this.tech_spec = tech_spec;
        this.add_info = add_info;
    }

    public int getError_id() {
        return error_id;
    }

    public int getPrice() {
        return price;
    }

    public ArrayList<String> getTech_spec() {
        return tech_spec;
    }

    public ArrayList<String> getAdd_info() {
        return add_info;
    }

    public void setError_id(int error_id) {
        this.error_id = error_id;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setTech_spec(ArrayList<String> tech_spec) {
        this.tech_spec = tech_spec;
    }

    public void setAdd_info(ArrayList<String> add_info) {
        this.add_info = add_info;
    }
}
