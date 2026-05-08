package id.ac.ui.cs.advprog.order.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InventoryProductResponse {

    private String id;
    private String nama;
    private String deskripsi;
    private BigDecimal harga;
    private Integer stok;
    private String negaraAsal;
    private LocalDate tanggalPembelian;
    private LocalDate tanggalKembali;
    private List<String> imageUrls;
    private String jastiperId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public BigDecimal getHarga() {
        return harga;
    }

    public void setHarga(BigDecimal harga) {
        this.harga = harga;
    }

    public Integer getStok() {
        return stok;
    }

    public void setStok(Integer stok) {
        this.stok = stok;
    }

    public String getNegaraAsal() {
        return negaraAsal;
    }

    public void setNegaraAsal(String negaraAsal) {
        this.negaraAsal = negaraAsal;
    }

    public LocalDate getTanggalPembelian() {
        return tanggalPembelian;
    }

    public void setTanggalPembelian(LocalDate tanggalPembelian) {
        this.tanggalPembelian = tanggalPembelian;
    }

    public LocalDate getTanggalKembali() {
        return tanggalKembali;
    }

    public void setTanggalKembali(LocalDate tanggalKembali) {
        this.tanggalKembali = tanggalKembali;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getJastiperId() {
        return jastiperId;
    }

    public void setJastiperId(String jastiperId) {
        this.jastiperId = jastiperId;
    }
}