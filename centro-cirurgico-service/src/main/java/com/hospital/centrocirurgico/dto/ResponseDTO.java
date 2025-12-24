package com.hospital.centrocirurgico.dto;

public class ResponseDTO {
    private String mensagem;
    private String codigoExame;
    private String codigo;

    public ResponseDTO() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String mensagem;
        private String codigoExame;
        private String codigo;

        public Builder mensagem(String mensagem) { this.mensagem = mensagem; return this; }
        public Builder codigoExame(String codigoExame) { this.codigoExame = codigoExame; return this; }
        public Builder codigo(String codigo) { this.codigo = codigo; return this; }
        public ResponseDTO build() {
            ResponseDTO r = new ResponseDTO();
            r.mensagem = this.mensagem;
            r.codigoExame = this.codigoExame;
            r.codigo = this.codigo;
            return r;
        }
    }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getCodigoExame() { return codigoExame; }
    public void setCodigoExame(String codigoExame) { this.codigoExame = codigoExame; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}
