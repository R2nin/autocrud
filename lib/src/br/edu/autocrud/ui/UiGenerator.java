package br.edu.autocrud.ui;

import br.edu.autocrud.core.EntityMetadata;
import br.edu.autocrud.core.EntityMetadata.ColumnMetadata;

import java.util.List;
import java.util.stream.Collectors;

public class UiGenerator {

    private static final int DIALOG_THRESHOLD = 5;

    public static String generate(List<EntityMetadata> entities) {
        StringBuilder sb = new StringBuilder(120_000);
        sb.append("<!DOCTYPE html>\n<html lang=\"pt-BR\">\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n");
        sb.append("<title>AutoCrud</title>\n<style>\n");
        css(sb);
        sb.append("</style>\n</head>\n<body>\n");
        skeleton(sb);
        sb.append("<script>\n");
        maskEngine(sb);
        js(sb, entities);
        sb.append("</script>\n</body>\n</html>\n");
        return sb.toString();
    }

    private static void css(StringBuilder s) {
        s.append("@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');\n");
        s.append("*{box-sizing:border-box;margin:0;padding:0}\n");
        s.append(":root{\n");
        s.append("--accent:#6366f1;--accent-d:#4f46e5;--accent-dd:#3730a3;--accent-l:#eef2ff;--accent-ll:#f0f4ff;\n");
        s.append("--success:#10b981;--success-l:#ecfdf5;--danger:#ef4444;--danger-l:#fef2f2;--warn:#f59e0b;--warn-l:#fffbeb;\n");
        s.append("--g0:#fff;--g50:#f8fafc;--g100:#f1f5f9;--g150:#e8edf5;--g200:#e2e8f0;--g300:#cbd5e1;\n");
        s.append("--g400:#94a3b8;--g500:#64748b;--g600:#475569;--g700:#334155;--g800:#1e293b;--g900:#0f172a;\n");
        s.append("--sb:#0f172a;--sb2:#1e293b;--sb3:#263148;--sbtext:#94a3b8;--sbact:#a5b4fc;\n");
        s.append("--r:8px;--r2:12px;--r3:16px;\n");
        s.append("--sh:0 1px 2px rgba(0,0,0,.05);\n");
        s.append("--sh2:0 4px 6px -1px rgba(0,0,0,.08),0 2px 4px -1px rgba(0,0,0,.04);\n");
        s.append("--sh3:0 10px 25px -5px rgba(0,0,0,.12),0 4px 10px -5px rgba(0,0,0,.06);\n");
        s.append("--sh4:0 20px 40px -10px rgba(0,0,0,.18),0 8px 16px -8px rgba(0,0,0,.1);\n");
        s.append("}\n");
        s.append("html,body{height:100%;overflow:hidden}\n");
        s.append("body{font-family:'Inter',-apple-system,BlinkMacSystemFont,sans-serif;font-size:14px;background:var(--g100);color:var(--g800);display:flex}\n");

        s.append("#sb{width:260px;background:var(--sb);display:flex;flex-direction:column;flex-shrink:0;overflow:hidden;border-right:1px solid var(--sb2)}\n");
        s.append("#sb-logo{padding:18px 20px;display:flex;align-items:center;gap:12px;border-bottom:1px solid var(--sb2);flex-shrink:0}\n");
        s.append(".logo-gem{width:36px;height:36px;background:linear-gradient(135deg,#6366f1,#8b5cf6);border-radius:10px;display:flex;align-items:center;justify-content:center;box-shadow:0 4px 12px rgba(99,102,241,.4);flex-shrink:0}\n");
        s.append(".logo-gem svg{color:#fff}\n");
        s.append(".logo-name{font-size:15px;font-weight:700;color:#f1f5f9;letter-spacing:-.3px}\n");
        s.append(".logo-ver{font-size:10px;color:var(--sbtext);font-weight:500;letter-spacing:.3px;text-transform:uppercase;margin-top:1px}\n");
        s.append("#sb-scroll{flex:1;overflow-y:auto;padding:8px 0 16px}\n");
        s.append("#sb-scroll::-webkit-scrollbar{width:4px}\n");
        s.append("#sb-scroll::-webkit-scrollbar-track{background:transparent}\n");
        s.append("#sb-scroll::-webkit-scrollbar-thumb{background:var(--sb3);border-radius:4px}\n");
        s.append(".sb-label{padding:12px 20px 6px;font-size:10px;font-weight:700;color:#475569;text-transform:uppercase;letter-spacing:1.2px}\n");
        s.append(".sb-item{display:flex;align-items:center;gap:10px;padding:9px 16px 9px 20px;cursor:pointer;color:var(--sbtext);font-size:13px;font-weight:500;border-left:3px solid transparent;transition:all .15s;user-select:none;position:relative}\n");
        s.append(".sb-item:hover{background:rgba(255,255,255,.04);color:#cbd5e1}\n");
        s.append(".sb-item.active{background:rgba(99,102,241,.12);color:var(--sbact);border-left-color:var(--accent)}\n");
        s.append(".sb-icon{width:16px;height:16px;flex-shrink:0;opacity:.6;transition:opacity .15s}\n");
        s.append(".sb-item:hover .sb-icon,.sb-item.active .sb-icon{opacity:1}\n");
        s.append(".sb-divider{height:1px;background:var(--sb2);margin:8px 16px}\n");

        s.append("#main{flex:1;display:flex;flex-direction:column;overflow:hidden;min-width:0}\n");
        s.append("#topbar{background:var(--g0);border-bottom:1px solid var(--g150);height:64px;padding:0 32px;display:flex;align-items:center;gap:16px;flex-shrink:0;box-shadow:var(--sh)}\n");
        s.append(".tb-ent-icon{width:40px;height:40px;border-radius:var(--r2);display:flex;align-items:center;justify-content:center;flex-shrink:0}\n");
        s.append(".tb-ent-icon.list{background:var(--accent-l);color:var(--accent)}\n");
        s.append(".tb-ent-icon.form{background:#f0fdf4;color:#16a34a}\n");
        s.append(".tb-texts{flex:1;min-width:0}\n");
        s.append(".tb-title{font-size:16px;font-weight:700;color:var(--g900);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}\n");
        s.append(".tb-crumb{font-size:12px;color:var(--g400);margin-top:1px}\n");
        s.append(".tb-right{display:flex;align-items:center;gap:10px}\n");
        s.append("#content{flex:1;overflow-y:auto;padding:32px}\n");
        s.append("#content::-webkit-scrollbar{width:6px}\n");
        s.append("#content::-webkit-scrollbar-track{background:transparent}\n");
        s.append("#content::-webkit-scrollbar-thumb{background:var(--g200);border-radius:4px}\n");

        s.append(".card{background:var(--g0);border-radius:var(--r2);box-shadow:var(--sh);border:1px solid var(--g150);overflow:hidden}\n");
        s.append(".card-head{padding:18px 24px;border-bottom:1px solid var(--g100);display:flex;align-items:center;gap:12px}\n");
        s.append(".card-head-title{font-size:15px;font-weight:600;color:var(--g900);flex:1}\n");
        s.append(".card-head-badge{font-size:11px;font-weight:600;color:var(--g500);background:var(--g100);padding:3px 10px;border-radius:20px;border:1px solid var(--g200)}\n");
        s.append(".card-body{padding:0}\n");

        s.append(".tbl-scroll{overflow-x:auto}\n");
        s.append("table{width:100%;border-collapse:collapse;font-size:13px}\n");
        s.append("thead{position:sticky;top:0;z-index:1}\n");
        s.append("th{padding:10px 20px;text-align:left;font-size:11px;font-weight:700;color:var(--g500);text-transform:uppercase;letter-spacing:.7px;background:var(--g50);border-bottom:1px solid var(--g200);white-space:nowrap}\n");
        s.append("td{padding:13px 20px;border-bottom:1px solid var(--g100);color:var(--g700);vertical-align:middle;line-height:1.4}\n");
        s.append("tr:last-child td{border-bottom:none}\n");
        s.append("tbody tr{transition:background .12s}\n");
        s.append("tbody tr:hover td{background:var(--g50)}\n");
        s.append(".td-actions{display:flex;gap:6px;align-items:center;opacity:0;transition:opacity .15s}\n");
        s.append("tr:hover .td-actions{opacity:1}\n");
        s.append(".cell-clip{max-width:220px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;display:inline-block;vertical-align:middle}\n");
        s.append(".cell-null{color:var(--g300);font-style:italic;font-size:12px}\n");

        s.append(".badge{display:inline-flex;align-items:center;padding:2px 10px;border-radius:20px;font-size:11px;font-weight:700;letter-spacing:.1px}\n");
        s.append(".badge-id{background:#ede9fe;color:#6d28d9;border:1px solid #ddd6fe}\n");
        s.append(".badge-count{background:var(--g100);color:var(--g600);border:1px solid var(--g200)}\n");
        s.append(".badge-success{background:var(--success-l);color:#059669;border:1px solid #a7f3d0}\n");
        s.append(".badge-warn{background:var(--warn-l);color:#d97706;border:1px solid #fde68a}\n");

        s.append(".btn{display:inline-flex;align-items:center;gap:7px;padding:8px 16px;border-radius:var(--r);border:none;cursor:pointer;font-size:13px;font-weight:500;transition:all .15s;white-space:nowrap;font-family:inherit;line-height:1.2}\n");
        s.append(".btn:active{transform:scale(.98)}\n");
        s.append(".btn:disabled{opacity:.45;cursor:not-allowed;transform:none}\n");
        s.append(".btn-primary{background:var(--accent);color:#fff;box-shadow:0 1px 3px rgba(99,102,241,.3)}\n");
        s.append(".btn-primary:hover:not(:disabled){background:var(--accent-d);box-shadow:0 4px 10px rgba(99,102,241,.35)}\n");
        s.append(".btn-danger{background:var(--danger-l);color:var(--danger);border:1px solid #fecaca}\n");
        s.append(".btn-danger:hover:not(:disabled){background:#fee2e2}\n");
        s.append(".btn-ghost{background:transparent;color:var(--g600);border:1px solid var(--g200)}\n");
        s.append(".btn-ghost:hover:not(:disabled){background:var(--g50);color:var(--g800)}\n");
        s.append(".btn-text{background:transparent;color:var(--accent);padding:6px 10px;border:none}\n");
        s.append(".btn-text:hover{background:var(--accent-l)}\n");
        s.append(".btn-sm{padding:5px 12px;font-size:12px}\n");
        s.append(".btn-xs{padding:4px 10px;font-size:11px;gap:4px}\n");
        s.append(".btn-icon-only{padding:7px;border-radius:var(--r);width:34px;height:34px;justify-content:center}\n");

        s.append(".form-wrap{max-width:800px}\n");
        s.append(".form-section{margin-bottom:28px}\n");
        s.append(".form-section-title{font-size:12px;font-weight:700;color:var(--g500);text-transform:uppercase;letter-spacing:.8px;margin-bottom:16px;padding-bottom:10px;border-bottom:1px solid var(--g150)}\n");
        s.append(".form-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:20px 24px}\n");
        s.append(".fg{display:flex;flex-direction:column;gap:6px}\n");
        s.append(".fg-full{grid-column:1/-1}\n");
        s.append(".fg>label{font-size:12px;font-weight:600;color:var(--g600);display:flex;align-items:center;gap:5px;user-select:none}\n");
        s.append(".req-star{color:var(--danger);font-size:14px;line-height:1;margin-top:-1px}\n");
        s.append(".fg .input-wrap{position:relative;display:flex;align-items:center}\n");
        s.append(".fg .input-prefix{position:absolute;left:12px;color:var(--g400);font-size:13px;pointer-events:none;font-weight:500;z-index:1}\n");
        s.append(".fg .has-prefix input{padding-left:28px}\n");
        s.append(".fg .input-status{position:absolute;right:12px;display:flex;align-items:center;pointer-events:none}\n");
        s.append(".fg input,.fg select,.fg textarea{padding:10px 14px;border:1.5px solid var(--g200);border-radius:var(--r);font-size:14px;color:var(--g900);background:var(--g0);outline:none;transition:border-color .15s,box-shadow .15s,background .15s;width:100%;font-family:inherit}\n");
        s.append(".fg input::placeholder,.fg textarea::placeholder{color:var(--g300);font-weight:400}\n");
        s.append(".fg input:hover,.fg select:hover,.fg textarea:hover{border-color:var(--g300)}\n");
        s.append(".fg input:focus,.fg select:focus,.fg textarea:focus{border-color:var(--accent);box-shadow:0 0 0 3px rgba(99,102,241,.1);background:var(--g0)}\n");
        s.append(".fg input.err{border-color:var(--danger)!important;box-shadow:0 0 0 3px rgba(239,68,68,.08)!important}\n");
        s.append(".fg input.ok{border-color:var(--success)}\n");
        s.append(".fg select{cursor:pointer}\n");
        s.append(".fg textarea{resize:vertical;min-height:88px;line-height:1.6}\n");
        s.append(".field-msg{font-size:11.5px;display:flex;align-items:flex-start;gap:4px;min-height:16px;line-height:1.4}\n");
        s.append(".field-msg.err-msg{color:var(--danger)}\n");
        s.append(".field-msg.hint-msg{color:var(--g400)}\n");
        s.append(".mask-label{font-size:10px;font-weight:600;background:var(--accent-l);color:var(--accent);padding:2px 7px;border-radius:20px;letter-spacing:.3px}\n");
        s.append(".toggle-row{display:flex;align-items:center;gap:10px;padding:10px 0}\n");
        s.append(".toggle-row input[type=checkbox]{width:18px;height:18px;cursor:pointer;accent-color:var(--accent);border-radius:4px}\n");
        s.append(".toggle-label{font-size:14px;color:var(--g700);cursor:pointer}\n");
        s.append(".form-footer{margin-top:32px;padding-top:20px;border-top:1px solid var(--g150);display:flex;gap:10px;align-items:center}\n");
        s.append(".form-footer .required-note{font-size:12px;color:var(--g400);margin-left:auto}\n");
        s.append(".err-banner{background:var(--danger-l);border:1px solid #fca5a5;border-radius:var(--r);padding:12px 16px;margin-bottom:20px;font-size:13px;color:#b91c1c;display:none;align-items:flex-start;gap:10px}\n");
        s.append(".err-banner.show{display:flex}\n");

        s.append(".overlay{position:fixed;inset:0;background:rgba(15,23,42,.55);display:flex;align-items:center;justify-content:center;z-index:800;opacity:0;pointer-events:none;transition:opacity .2s;backdrop-filter:blur(3px)}\n");
        s.append(".overlay.open{opacity:1;pointer-events:all}\n");
        s.append(".modal{background:var(--g0);border-radius:var(--r3);box-shadow:var(--sh4);border:1px solid var(--g150);display:flex;flex-direction:column;transform:scale(.95) translateY(16px);transition:transform .22s cubic-bezier(.34,1.3,.64,1)}\n");
        s.append(".overlay.open .modal{transform:scale(1) translateY(0)}\n");
        s.append(".modal-lg{width:min(700px,96vw);max-height:90vh}\n");
        s.append(".modal-sm{width:min(420px,96vw)}\n");
        s.append(".modal-head{padding:20px 24px 18px;border-bottom:1px solid var(--g100);display:flex;align-items:flex-start;justify-content:space-between;gap:12px;flex-shrink:0}\n");
        s.append(".modal-head-left h2{font-size:17px;font-weight:700;color:var(--g900)}\n");
        s.append(".modal-head-left .modal-sub{font-size:12px;color:var(--g400);margin-top:3px}\n");
        s.append(".modal-x{width:30px;height:30px;border:none;background:var(--g100);border-radius:7px;cursor:pointer;display:flex;align-items:center;justify-content:center;color:var(--g500);font-size:17px;transition:all .15s;flex-shrink:0}\n");
        s.append(".modal-x:hover{background:var(--g200);color:var(--g800)}\n");
        s.append(".modal-scroll{overflow-y:auto;flex:1}\n");
        s.append(".modal-scroll::-webkit-scrollbar{width:4px}\n");
        s.append(".modal-scroll::-webkit-scrollbar-thumb{background:var(--g200);border-radius:4px}\n");
        s.append(".modal-foot{padding:14px 24px;border-top:1px solid var(--g100);display:flex;gap:8px;justify-content:flex-end;flex-shrink:0;background:var(--g50)}\n");

        s.append(".dtl-grid{padding:20px 24px;display:grid;grid-template-columns:1fr 1fr;gap:0}\n");
        s.append(".dtl-cell{padding:13px 0;border-bottom:1px solid var(--g100)}\n");
        s.append(".dtl-cell:nth-child(odd){padding-right:28px}\n");
        s.append(".dtl-cell:nth-child(even){padding-left:28px;border-left:1px solid var(--g100)}\n");
        s.append(".dtl-cell:nth-last-child(-n+2){border-bottom:none}\n");
        s.append(".dtl-key{font-size:10px;font-weight:700;color:var(--g400);text-transform:uppercase;letter-spacing:.8px;margin-bottom:5px;display:flex;align-items:center;gap:6px}\n");
        s.append(".dtl-val{font-size:14px;color:var(--g800);word-break:break-word;line-height:1.5}\n");
        s.append(".dtl-empty{color:var(--g300);font-style:italic;font-size:13px}\n");

        s.append(".del-body{padding:28px 24px 20px;text-align:center}\n");
        s.append(".del-icon-wrap{width:60px;height:60px;background:var(--danger-l);border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 18px;color:var(--danger)}\n");
        s.append(".del-body h3{font-size:17px;font-weight:700;color:var(--g900);margin-bottom:8px}\n");
        s.append(".del-body p{font-size:14px;color:var(--g500);line-height:1.6;max-width:300px;margin:0 auto}\n");

        s.append(".empty-state{padding:72px 32px;text-align:center}\n");
        s.append(".empty-state .empty-icon{width:64px;height:64px;background:var(--g100);border-radius:16px;display:flex;align-items:center;justify-content:center;margin:0 auto 20px;color:var(--g300)}\n");
        s.append(".empty-state h3{font-size:16px;font-weight:600;color:var(--g600);margin-bottom:6px}\n");
        s.append(".empty-state p{font-size:13px;color:var(--g400)}\n");

        s.append(".loader-wrap{padding:48px;display:flex;justify-content:center}\n");
        s.append(".loader{width:32px;height:32px;border:3px solid var(--g150);border-top-color:var(--accent);border-radius:50%;animation:spin .7s linear infinite}\n");
        s.append("@keyframes spin{to{transform:rotate(360deg)}}\n");

        s.append("#toast{position:fixed;bottom:28px;right:28px;background:var(--g900);color:#f8fafc;padding:12px 16px 12px 14px;border-radius:var(--r2);font-size:13px;font-weight:500;opacity:0;transform:translateY(10px) scale(.96);transition:all .25s cubic-bezier(.34,1.4,.64,1);z-index:9999;pointer-events:none;display:flex;align-items:center;gap:10px;box-shadow:var(--sh4);max-width:360px}\n");
        s.append("#toast.show{opacity:1;transform:translateY(0) scale(1)}\n");
        s.append(".t-bar{width:4px;height:32px;border-radius:4px;flex-shrink:0}\n");
        s.append(".t-bar.success{background:var(--success)}\n");
        s.append(".t-bar.error{background:var(--danger)}\n");
        s.append(".t-bar.info{background:var(--accent)}\n");
        s.append(".t-content{display:flex;flex-direction:column;gap:2px}\n");
        s.append(".t-label{font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.5px;color:var(--g400)}\n");
        s.append(".t-msg{font-size:13px;color:#f1f5f9}\n");

        s.append("*::-webkit-scrollbar{width:6px;height:6px}\n");
        s.append("*::-webkit-scrollbar-track{background:transparent}\n");
        s.append("*::-webkit-scrollbar-thumb{background:var(--g200);border-radius:4px}\n");
        s.append("*::-webkit-scrollbar-thumb:hover{background:var(--g300)}\n");

        s.append(".badge-accent{background:rgba(99,102,241,.1);color:var(--accent);border:1px solid rgba(99,102,241,.2)}\n");
        s.append(".eb-field-card{background:var(--g50);border:1px solid var(--g150);border-radius:var(--r2);margin-bottom:12px}\n");
        s.append(".eb-field-hd{display:flex;align-items:center;justify-content:space-between;padding:10px 16px;border-bottom:1px solid var(--g150)}\n");
        s.append(".eb-field-num{font-size:12px;font-weight:700;color:var(--accent);text-transform:uppercase;letter-spacing:.5px}\n");
        s.append(".eb-field-bd{padding:16px}\n");

        s.append("@media(max-width:900px){\n");
        s.append("#sb{width:220px}\n");
        s.append("#content{padding:20px}\n");
        s.append(".dtl-grid{grid-template-columns:1fr}\n");
        s.append(".dtl-cell:nth-child(even){padding-left:0;border-left:none}\n");
        s.append(".dtl-cell:nth-last-child(-n+2){border-bottom:1px solid var(--g100)}\n");
        s.append(".dtl-cell:last-child{border-bottom:none}\n");
        s.append("}\n");
    }

    private static void skeleton(StringBuilder s) {
        s.append("<nav id=\"sb\">\n");
        s.append("  <div id=\"sb-logo\">\n");
        s.append("    <div class=\"logo-gem\"><svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.5\"><path d=\"M13 2L3 14h9l-1 8 10-12h-9l1-8z\"/></svg></div>\n");
        s.append("    <div><div class=\"logo-name\">AutoCrud</div><div class=\"logo-ver\">Framework v1</div></div>\n");
        s.append("  </div>\n");
        s.append("  <div id=\"sb-scroll\"><div id=\"sb-nav\"></div></div>\n");
        s.append("</nav>\n");

        s.append("<div id=\"main\">\n");
        s.append("  <header id=\"topbar\">\n");
        s.append("    <div class=\"tb-ent-icon list\" id=\"tb-icon\"></div>\n");
        s.append("    <div class=\"tb-texts\">\n");
        s.append("      <div class=\"tb-title\" id=\"tb-title\">Carregando...</div>\n");
        s.append("      <div class=\"tb-crumb\" id=\"tb-crumb\"></div>\n");
        s.append("    </div>\n");
        s.append("    <div class=\"tb-right\" id=\"tb-right\"></div>\n");
        s.append("  </header>\n");
        s.append("  <div id=\"content\"></div>\n");
        s.append("</div>\n");

        s.append("<div class=\"overlay\" id=\"dtl-ov\" onclick=\"handleOvClick(event,'dtl-ov')\">\n");
        s.append("  <div class=\"modal modal-lg\">\n");
        s.append("    <div class=\"modal-head\">\n");
        s.append("      <div class=\"modal-head-left\"><h2 id=\"dtl-h\"></h2><div class=\"modal-sub\" id=\"dtl-sub\"></div></div>\n");
        s.append("      <button class=\"modal-x\" onclick=\"closeOv('dtl-ov')\">&#215;</button>\n");
        s.append("    </div>\n");
        s.append("    <div class=\"modal-scroll\"><div class=\"dtl-grid\" id=\"dtl-body\"></div></div>\n");
        s.append("    <div class=\"modal-foot\">\n");
        s.append("      <button class=\"btn btn-ghost\" onclick=\"closeOv('dtl-ov')\">Fechar</button>\n");
        s.append("      <button class=\"btn btn-primary\" id=\"dtl-edit\">Editar Registro</button>\n");
        s.append("    </div>\n");
        s.append("  </div>\n");
        s.append("</div>\n");

        s.append("<div class=\"overlay\" id=\"del-ov\" onclick=\"handleOvClick(event,'del-ov')\">\n");
        s.append("  <div class=\"modal modal-sm\">\n");
        s.append("    <div class=\"del-body\">\n");
        s.append("      <div class=\"del-icon-wrap\"><svg width=\"28\" height=\"28\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><polyline points=\"3 6 5 6 21 6\"/><path d=\"M19 6l-1 14H6L5 6\"/><path d=\"M10 11v6\"/><path d=\"M14 11v6\"/><path d=\"M9 6V4h6v2\"/></svg></div>\n");
        s.append("      <h3>Excluir registro</h3>\n");
        s.append("      <p id=\"del-msg\">Esta a&#231;&#227;o n&#227;o pode ser desfeita.</p>\n");
        s.append("    </div>\n");
        s.append("    <div class=\"modal-foot\">\n");
        s.append("      <button class=\"btn btn-ghost\" onclick=\"closeOv('del-ov')\">Cancelar</button>\n");
        s.append("      <button class=\"btn btn-danger\" id=\"del-btn\">Excluir</button>\n");
        s.append("    </div>\n");
        s.append("  </div>\n");
        s.append("</div>\n");

        s.append("<div id=\"toast\"><div class=\"t-bar info\" id=\"t-bar\"></div><div class=\"t-content\"><div class=\"t-label\" id=\"t-lbl\"></div><div class=\"t-msg\" id=\"t-msg\"></div></div></div>\n");
    }

    private static void maskEngine(StringBuilder s) {
        s.append("/* ── Mask engine ── */\n");
        s.append("var MASK_ALIASES={\n");
        s.append("  CPF:'000.000.000-00',\n");
        s.append("  CNPJ:'00.000.000/0000-00',\n");
        s.append("  TELEFONE:'(00) 0000-0000',\n");
        s.append("  CELULAR:'(00) 00000-0000',\n");
        s.append("  CEP:'00000-000',\n");
        s.append("  DATA:'00/00/0000',\n");
        s.append("  HORA:'00:00',\n");
        s.append("  CARTAO:'0000 0000 0000 0000',\n");
        s.append("  DINHEIRO:'DINHEIRO'\n");
        s.append("};\n\n");

        s.append("function resolveMask(m){return MASK_ALIASES[m.toUpperCase()]||m;}\n\n");

        s.append("function applyMask(raw,pattern){\n");
        s.append("  if(!pattern||pattern==='DINHEIRO')return raw;\n");
        s.append("  var digits=raw.replace(/\\D/g,'');\n");
        s.append("  var letters=raw.replace(/[^a-zA-Z]/g,'');\n");
        s.append("  var di=0,li=0,out='';\n");
        s.append("  for(var pi=0;pi<pattern.length;pi++){\n");
        s.append("    var pc=pattern[pi];\n");
        s.append("    if(pc==='0'){if(di<digits.length)out+=digits[di++];else break;}\n");
        s.append("    else if(pc==='A'){if(li<letters.length)out+=letters[li++];else break;}\n");
        s.append("    else if(pc==='S'){var c=raw[di+li];if(c)out+=c;else break;}\n");
        s.append("    else out+=pc;\n");
        s.append("  }\n");
        s.append("  return out;\n");
        s.append("}\n\n");

        s.append("function applyDinheiro(raw){\n");
        s.append("  var digits=raw.replace(/\\D/g,'');\n");
        s.append("  if(!digits)return '';\n");
        s.append("  var n=parseInt(digits,10)/100;\n");
        s.append("  return n.toLocaleString('pt-BR',{minimumFractionDigits:2,maximumFractionDigits:2});\n");
        s.append("}\n\n");

        s.append("function stripMask(val,pattern){\n");
        s.append("  if(!pattern)return val;\n");
        s.append("  if(pattern==='DINHEIRO')return val.replace(/\\./g,'').replace(',','.');\n");
        s.append("  return val.replace(/\\D/g,'');\n");
        s.append("}\n\n");

        s.append("function bindMask(input,rawMask){\n");
        s.append("  if(!rawMask)return;\n");
        s.append("  var pattern=resolveMask(rawMask);\n");
        s.append("  input.dataset.mask=pattern;\n");
        s.append("  input.dataset.rawMask=rawMask;\n");
        s.append("  function fmt(e){\n");
        s.append("    var pos=input.selectionStart;\n");
        s.append("    var old=input.value;\n");
        s.append("    var nv=pattern==='DINHEIRO'?applyDinheiro(input.value):applyMask(input.value,pattern);\n");
        s.append("    input.value=nv;\n");
        s.append("    if(e&&e.type==='input'){\n");
        s.append("      var diff=nv.length-old.length;\n");
        s.append("      try{input.setSelectionRange(pos+diff,pos+diff);}catch(ex){}\n");
        s.append("    }\n");
        s.append("  }\n");
        s.append("  input.addEventListener('input',fmt);\n");
        s.append("  input.addEventListener('focus',fmt);\n");
        s.append("  if(input.value)fmt(null);\n");
        s.append("}\n\n");
    }

    private static void js(StringBuilder s, List<EntityMetadata> entities) {
        s.append("var ENT=").append(buildMetaJson(entities)).append(";\n");
        s.append("var THR=").append(DIALOG_THRESHOLD).append(";\n");
        s.append("var _ei=0,_rows={};\n\n");

        s.append("function buildNav(){\n");
        s.append("  var nav=document.getElementById('sb-nav'),h='';\n");
        s.append("  ENT.forEach(function(e,i){\n");
        s.append("    h+='<div class=\"sb-label\">'+escH(e.label)+'</div>';\n");
        s.append("    h+='<div class=\"sb-item\" id=\"si-'+i+'-list\" onclick=\"go('+i+',\\'list\\')\">';\n");
        s.append("    h+='<svg class=\"sb-icon\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><rect x=\"3\" y=\"3\" width=\"18\" height=\"18\" rx=\"2\"/><line x1=\"3\" y1=\"9\" x2=\"21\" y2=\"9\"/><line x1=\"3\" y1=\"15\" x2=\"21\" y2=\"15\"/><line x1=\"9\" y1=\"3\" x2=\"9\" y2=\"21\"/></svg>';\n");
        s.append("    h+=' Listar registros</div>';\n");
        s.append("    h+='<div class=\"sb-item\" id=\"si-'+i+'-form\" onclick=\"go('+i+',\\'form\\')\">';\n");
        s.append("    h+='<svg class=\"sb-icon\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><line x1=\"12\" y1=\"5\" x2=\"12\" y2=\"19\"/><line x1=\"5\" y1=\"12\" x2=\"19\" y2=\"12\"/></svg>';\n");
        s.append("    h+=' Novo registro</div>';\n");
        s.append("    h+='<div class=\"sb-divider\"></div>';\n");
        s.append("  });\n");
        s.append("  h+='<div class=\"sb-divider\"></div>';\n");
        s.append("  h+='<div class=\"sb-item\" id=\"si-eb\" onclick=\"goEntityBuilder()\">';\n");
        s.append("  h+='<svg class=\"sb-icon\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z\"/><polyline points=\"3.27 6.96 12 12.01 20.73 6.96\"/><line x1=\"12\" y1=\"22.08\" x2=\"12\" y2=\"12\"/></svg>';\n");
        s.append("  h+=' Nova Entidade</div>';\n");
        s.append("  nav.innerHTML=h;\n");
        s.append("}\n\n");

        s.append("function go(i,view,id){\n");
        s.append("  _ei=i;\n");
        s.append("  document.querySelectorAll('.sb-item').forEach(function(el){el.classList.remove('active');});\n");
        s.append("  var si=document.getElementById('si-'+i+'-'+(view==='form'?'form':'list'));\n");
        s.append("  if(si)si.classList.add('active');\n");
        s.append("  var e=ENT[i];\n");
        s.append("  var icon=document.getElementById('tb-icon');\n");
        s.append("  icon.className='tb-ent-icon '+(view==='list'?'list':'form');\n");
        s.append("  if(view==='list')icon.innerHTML=svgList();else icon.innerHTML=svgForm();\n");
        s.append("  document.getElementById('tb-title').textContent=e.label;\n");
        s.append("  document.getElementById('tb-crumb').textContent=view==='list'?'Todos os registros':(id?'Editando #'+id:'Novo registro');\n");
        s.append("  var tr=document.getElementById('tb-right');\n");
        s.append("  if(view==='list')tr.innerHTML='<button class=\"btn btn-primary\" onclick=\"go('+i+',\\'form\\')\">'+svgPlus()+' Novo '+escH(e.label)+'</button>';\n");
        s.append("  else tr.innerHTML='<button class=\"btn btn-ghost\" onclick=\"go('+i+',\\'list\\')\">' + svgBack() + ' Voltar</button>';\n");
        s.append("  if(view==='list')renderList(i);\n");
        s.append("  else renderForm(i,id);\n");
        s.append("}\n\n");

        s.append("async function renderList(i){\n");
        s.append("  var e=ENT[i],ct=document.getElementById('content');\n");
        s.append("  ct.innerHTML='<div class=\"card\"><div class=\"loader-wrap\"><div class=\"loader\"></div></div></div>';\n");
        s.append("  var rows;\n");
        s.append("  try{rows=await req(e.apiPath);}catch(err){\n");
        s.append("    ct.innerHTML='<div class=\"card\"><div class=\"empty-state\"><div class=\"empty-icon\">'+svgWarn()+'</div><h3>Erro ao carregar</h3><p>'+escH(err.message)+'</p></div></div>';\n");
        s.append("    return;\n");
        s.append("  }\n");
        s.append("  var cols=e.columns,many=cols.length>THR,inline=many?cols.slice(0,4):cols;\n");
        s.append("  var h='<div class=\"card\">';\n");
        s.append("  h+='<div class=\"card-head\"><span class=\"card-head-title\">'+escH(e.label)+'</span>';\n");
        s.append("  h+='<span class=\"card-head-badge\">'+rows.length+' registro'+(rows.length!==1?'s':'')+'</span></div>';\n");
        s.append("  h+='<div class=\"card-body\">';\n");
        s.append("  if(rows.length===0){\n");
        s.append("    h+='<div class=\"empty-state\"><div class=\"empty-icon\">'+svgEmpty()+'</div>';\n");
        s.append("    h+='<h3>Nenhum registro ainda</h3><p>Clique em &ldquo;Novo '+escH(e.label)+'&rdquo; para come&#231;ar.</p></div>';\n");
        s.append("  } else {\n");
        s.append("    h+='<div class=\"tbl-scroll\"><table><thead><tr>';\n");
        s.append("    h+='<th style=\"width:56px\">ID</th>';\n");
        s.append("    inline.forEach(function(c){h+='<th>'+escH(c.label)+'</th>';});\n");
        s.append("    if(many)h+='<th style=\"width:90px\"></th>';\n");
        s.append("    h+='<th style=\"width:130px;text-align:right\">A&#231;&#245;es</th>';\n");
        s.append("    h+='</tr></thead><tbody>';\n");
        s.append("    _rows={};\n");
        s.append("    rows.forEach(function(row){\n");
        s.append("      _rows[row.id]=row;\n");
        s.append("      h+='<tr>';\n");
        s.append("      h+='<td><span class=\"badge badge-id\">'+row.id+'</span></td>';\n");
        s.append("      inline.forEach(function(c){\n");
        s.append("        var v=gv(row,c);\n");
        s.append("        var disp=(v===null||v===undefined||v==='')\n");
        s.append("          ?'<span class=\"cell-null\">—</span>'\n");
        s.append("          :'<span class=\"cell-clip\" title=\"'+escA(String(v))+'\">'+escH(String(v))+'</span>';\n");
        s.append("        h+='<td>'+disp+'</td>';\n");
        s.append("      });\n");
        s.append("      if(many)h+='<td><button class=\"btn btn-xs btn-text\" onclick=\"openDtl('+i+','+row.id+')\">Ver detalhes &#8594;</button></td>';\n");
        s.append("      h+='<td><div class=\"td-actions\" style=\"justify-content:flex-end\">';\n");
        s.append("      h+='<button class=\"btn btn-xs btn-ghost\" title=\"Editar\" onclick=\"go('+i+',\\'form\\','+row.id+')\">'+svgEdit()+' Editar</button>';\n");
        s.append("      h+='<button class=\"btn btn-xs btn-danger btn-icon-only\" title=\"Excluir\" onclick=\"openDel('+i+','+row.id+')\">'+svgTrash()+'</button>';\n");
        s.append("      h+='</div></td></tr>';\n");
        s.append("    });\n");
        s.append("    h+='</tbody></table></div>';\n");
        s.append("  }\n");
        s.append("  h+='</div></div>';\n");
        s.append("  ct.innerHTML=h;\n");
        s.append("}\n\n");

        s.append("async function renderForm(i,editId){\n");
        s.append("  var e=ENT[i],ct=document.getElementById('content');\n");
        s.append("  ct.innerHTML='<div class=\"card\"><div class=\"loader-wrap\"><div class=\"loader\"></div></div></div>';\n");
        s.append("  var exist={};\n");
        s.append("  if(editId){\n");
        s.append("    try{exist=await req(e.apiPath+'/'+editId);}catch(err){\n");
        s.append("      toast('Erro ao carregar','error');\n");
        s.append("      return;\n");
        s.append("    }\n");
        s.append("  }\n");
        s.append("  var eid=editId||null;\n");
        s.append("  var titleHtml=eid\n");
        s.append("    ?escH(e.label)+' <span class=\"badge badge-id\" style=\"vertical-align:middle;font-size:12px\">#'+eid+'</span>'\n");
        s.append("    :'Novo '+escH(e.label);\n");
        s.append("  var h='<div class=\"card\">';\n");
        s.append("  h+='<div class=\"card-head\"><span class=\"card-head-title\">'+titleHtml+'</span>';\n");
        s.append("  h+=eid?'<span class=\"card-head-badge badge-warn\">Editando</span>':'<span class=\"card-head-badge badge-success\">Novo</span>';\n");
        s.append("  h+='</div><div class=\"card-body\" style=\"padding:24px\">';\n");
        s.append("  h+='<div class=\"form-wrap\">';\n");
        s.append("  h+='<div class=\"err-banner\" id=\"fb\"><svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" style=\"flex-shrink:0\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><line x1=\"12\" y1=\"8\" x2=\"12\" y2=\"12\"/><line x1=\"12\" y1=\"16\" x2=\"12.01\" y2=\"16\"/></svg><span id=\"fb-msg\"></span></div>';\n");
        s.append("  h+='<form id=\"cf\" onsubmit=\"doSubmit(event,'+i+','+(eid||'null')+')\" novalidate>';\n");
        s.append("  h+='<div class=\"form-grid\">';\n");

        s.append("  e.columns.forEach(function(col){\n");
        s.append("    var v=gv(exist,col),vs=v===null||v===undefined?'':String(v);\n");
        s.append("    var isBool=col.sqlType.toUpperCase()==='BOOLEAN';\n");
        s.append("    var isLongText=(col.maxLength===0&&col.sqlType.toUpperCase().indexOf('VARCHAR')>=0);\n");
        s.append("    var cls='fg'+(isLongText||isBool?' fg-full':'');\n");
        s.append("    h+='<div class=\"'+cls+'\" id=\"fg-'+col.javaName+'\">';\n");
        s.append("    // label\n");
        s.append("    if(!isBool){\n");
        s.append("      h+='<label for=\"f-'+col.javaName+'\">'+escH(col.label);\n");
        s.append("      if(col.required)h+='<span class=\"req-star\">*</span>';\n");
        s.append("      if(col.mask)h+='<span class=\"mask-label\">'+escH(col.mask.toUpperCase())+'</span>';\n");
        s.append("      h+='</label>';\n");
        s.append("    }\n");
        s.append("    // input\n");
        s.append("    if(isBool){\n");
        s.append("      h+='<div class=\"toggle-row\"><input type=\"checkbox\" id=\"f-'+col.javaName+'\" name=\"'+col.javaName+'\"'+(vs==='true'?' checked':'')+'>'\n");
        s.append("        +'<label for=\"f-'+col.javaName+'\" class=\"toggle-label\">'+escH(col.label)+'</label></div>';\n");
        s.append("    } else {\n");
        s.append("      var it=col.mask?'text':inferType(col.sqlType);\n");
        s.append("      var ph=col.placeholder?col.placeholder:col.label;\n");
        s.append("      var extras='';\n");
        s.append("      if(col.min)extras+=' min=\"'+escA(col.min)+'\"';\n");
        s.append("      if(col.max)extras+=' max=\"'+escA(col.max)+'\"';\n");
        s.append("      if(col.maxLength>0)extras+=' maxlength=\"'+col.maxLength+'\"';\n");
        s.append("      var vid='f-'+col.javaName;\n");
        s.append("      h+='<input type=\"'+it+'\" id=\"'+vid+'\" name=\"'+col.javaName+'\"'\n");
        s.append("        +' value=\"'+escA(vs)+'\" placeholder=\"'+escA(ph)+'\"'\n");
        s.append("        +extras\n");
        s.append("        +' oninput=\"vField(this,\\''+col.javaName+'\\','+i+')\"'\n");
        s.append("        +' onblur=\"vField(this,\\''+col.javaName+'\\','+i+')\"'\n");
        s.append("        +'>';\n");
        s.append("    }\n");
        s.append("    // error + hint\n");
        s.append("    h+='<div class=\"field-msg err-msg\" id=\"fe-'+col.javaName+'\"></div>';\n");
        s.append("    if(col.minLength>0||col.maxLength>0){\n");
        s.append("      var hints=[];\n");
        s.append("      if(col.minLength>0)hints.push('m&#237;n. '+col.minLength);\n");
        s.append("      if(col.maxLength>0)hints.push('m&#225;x. '+col.maxLength+' caracteres');\n");
        s.append("      h+='<div class=\"field-msg hint-msg\">'+hints.join(' &middot; ')+'</div>';\n");
        s.append("    }\n");
        s.append("    h+='</div>';\n");
        s.append("  });\n");

        s.append("  h+='</div>';\n");
        s.append("  h+='<div class=\"form-footer\">';\n");
        s.append("  h+='<button type=\"submit\" class=\"btn btn-primary\" id=\"fsub\">'+svgCheck()+(eid?' Atualizar':' Salvar')+'</button>';\n");
        s.append("  h+='<button type=\"button\" class=\"btn btn-ghost\" onclick=\"go('+i+',\\'list\\')\">Cancelar</button>';\n");
        s.append("  h+='<span class=\"required-note\">* Campos obrigat&#243;rios</span>';\n");
        s.append("  h+='</div></form></div></div></div>';\n");
        s.append("  ct.innerHTML=h;\n");

        s.append("  e.columns.forEach(function(col){\n");
        s.append("    if(!col.mask)return;\n");
        s.append("    var el=document.getElementById('f-'+col.javaName);\n");
        s.append("    if(el)bindMask(el,col.mask);\n");
        s.append("  });\n");
        s.append("}\n\n");

        s.append("var _ebFields=[],_ebCls='',_ebLbl='';\n");
        s.append("function ebEmptyField(){\n");
        s.append("  return {name:'',type:'String',label:'',required:false,minLength:0,maxLength:0,\n");
        s.append("    min:'',max:'',placeholder:'',mask:'',pattern:'',errorMsg:'',sqlType:''};\n");
        s.append("}\n\n");

        s.append("function goEntityBuilder(){\n");
        s.append("  document.querySelectorAll('.sb-item').forEach(function(el){el.classList.remove('active');});\n");
        s.append("  var si=document.getElementById('si-eb');if(si)si.classList.add('active');\n");
        s.append("  var icon=document.getElementById('tb-icon');\n");
        s.append("  icon.className='tb-ent-icon form';\n");
        s.append("  icon.innerHTML=svgCube();\n");
        s.append("  document.getElementById('tb-title').textContent='Nova Entidade';\n");
        s.append("  document.getElementById('tb-crumb').textContent='Gerador de classe Java';\n");
        s.append("  document.getElementById('tb-right').innerHTML='';\n");
        s.append("  if(!_ebFields.length)_ebFields=[ebEmptyField()];\n");
        s.append("  renderEntityBuilder();\n");
        s.append("}\n\n");

        s.append("function ebSyncAll(){\n");
        s.append("  var ec=document.getElementById('eb-cls');if(ec)_ebCls=ec.value;\n");
        s.append("  var el=document.getElementById('eb-lbl');if(el)_ebLbl=el.value;\n");
        s.append("  _ebFields.forEach(function(f,i){\n");
        s.append("    function v(p){var e=document.getElementById(p+i);return e?e.value:'';}\n");
        s.append("    function vi(p){var e=document.getElementById(p+i);return e&&e.value?parseInt(e.value)||0:0;}\n");
        s.append("    function vc(p){var e=document.getElementById(p+i);return!!e&&e.checked;}\n");
        s.append("    f.name=v('ef-n-');f.type=v('ef-t-');f.label=v('ef-l-');\n");
        s.append("    f.mask=v('ef-m-');f.placeholder=v('ef-ph-');f.sqlType=v('ef-sq-');\n");
        s.append("    f.minLength=vi('ef-ml-');f.maxLength=vi('ef-xl-');\n");
        s.append("    f.min=v('ef-mi-');f.max=v('ef-ma-');\n");
        s.append("    f.pattern=v('ef-pa-');f.errorMsg=v('ef-em-');\n");
        s.append("    f.required=vc('ef-rq-');\n");
        s.append("  });\n");
        s.append("}\n\n");

        s.append("function renderEntityBuilder(){\n");
        s.append("  var ct=document.getElementById('content');\n");
        s.append("  var h='<div class=\"card\">';\n");
        s.append("  h+='<div class=\"card-head\"><span class=\"card-head-title\">Nova Entidade Java</span>';\n");
        s.append("  h+='<span class=\"card-head-badge badge-accent\">Gerador</span></div>';\n");
        s.append("  h+='<div class=\"card-body\" style=\"padding:24px\">';\n");
        s.append("  h+='<div class=\"form-grid\">';\n");
        s.append("  h+='<div class=\"fg\"><label>Nome da classe <span class=\"req-star\">*</span></label>';\n");
        s.append("  h+='<input id=\"eb-cls\" placeholder=\"ex: Fornecedor\" value=\"'+escA(_ebCls||'')+'\"></div>';\n");
        s.append("  h+='<div class=\"fg\"><label>Label (exibi\u00e7\u00e3o)</label>';\n");
        s.append("  h+='<input id=\"eb-lbl\" placeholder=\"ex: Fornecedor\" value=\"'+escA(_ebLbl||'')+'\"></div>';\n");
        s.append("  h+='</div>';\n");
        s.append("  h+='<div style=\"display:flex;align-items:center;justify-content:space-between;margin-top:24px;margin-bottom:12px\">';\n");
        s.append("  h+='<div style=\"font-size:15px;font-weight:700;color:var(--g800)\">Campos</div>';\n");
        s.append("  h+='<button class=\"btn btn-ghost\" onclick=\"ebSyncAll();_ebFields.push(ebEmptyField());renderEntityBuilder()\">'+svgPlus()+' Adicionar campo</button>';\n");
        s.append("  h+='</div>';\n");
        s.append("  _ebFields.forEach(function(f,i){\n");
        s.append("    h+='<div class=\"eb-field-card\">';\n");
        s.append("    h+='<div class=\"eb-field-hd\"><span class=\"eb-field-num\">Campo '+(i+1)+'</span>';\n");
        s.append("    if(_ebFields.length>1)\n");
        s.append("      h+='<button class=\"btn btn-xs btn-danger btn-icon-only\" title=\"Remover\" onclick=\"ebSyncAll();_ebFields.splice('+i+',1);renderEntityBuilder()\">'+svgTrash()+'</button>';\n");
        s.append("    h+='</div><div class=\"eb-field-bd\"><div class=\"form-grid\">';\n");
        s.append("    h+='<div class=\"fg\"><label>Nome (camelCase) <span class=\"req-star\">*</span></label>';\n");
        s.append("    h+='<input id=\"ef-n-'+i+'\" value=\"'+escA(f.name)+'\" placeholder=\"ex: nomeCompleto\"></div>';\n");
        s.append("    h+='<div class=\"fg\"><label>Tipo Java</label><select id=\"ef-t-'+i+'\">';\n");
        s.append("    ['String','Integer','Long','Double','Float','BigDecimal','Boolean','LocalDate','LocalDateTime'].forEach(function(t){\n");
        s.append("      h+='<option'+(f.type===t?' selected':'')+'>'+t+'</option>';\n");
        s.append("    });\n");
        s.append("    h+='</select></div>';\n");
        s.append("    h+='<div class=\"fg\"><label>Label <span class=\"req-star\">*</span></label>';\n");
        s.append("    h+='<input id=\"ef-l-'+i+'\" value=\"'+escA(f.label)+'\" placeholder=\"ex: Nome completo\"></div>';\n");
        s.append("    h+='<div class=\"fg\"><label>M\u00e1scara</label><select id=\"ef-m-'+i+'\">';\n");
        s.append("    ['','CPF','CNPJ','TELEFONE','CELULAR','CEP','DATA','HORA','CARTAO','DINHEIRO'].forEach(function(m){\n");
        s.append("      h+='<option value=\"'+m+'\"'+(f.mask===m?' selected':'')+'>'+(m||'Nenhuma')+'</option>';\n");
        s.append("    });\n");
        s.append("    h+='</select></div>';\n");
        s.append("    h+='<div class=\"fg\"><label>Placeholder</label>';\n");
        s.append("    h+='<input id=\"ef-ph-'+i+'\" value=\"'+escA(f.placeholder)+'\" placeholder=\"texto de ajuda\"></div>';\n");
        s.append("    h+='<div class=\"fg\"><label>SQL Type (opcional)</label>';\n");
        s.append("    h+='<input id=\"ef-sq-'+i+'\" value=\"'+escA(f.sqlType)+'\" placeholder=\"ex: DECIMAL(19,2)\"></div>';\n");
        s.append("    h+='<div class=\"fg\"><label>Min Length</label>';\n");
        s.append("    h+='<input type=\"number\" id=\"ef-ml-'+i+'\" value=\"'+(f.minLength||'')+'\" min=\"0\" placeholder=\"0\"></div>';\n");
        s.append("    h+='<div class=\"fg\"><label>Max Length</label>';\n");
        s.append("    h+='<input type=\"number\" id=\"ef-xl-'+i+'\" value=\"'+(f.maxLength||'')+'\" min=\"0\" placeholder=\"0\"></div>';\n");
        s.append("    h+='<div class=\"fg\"><label>M\u00ednimo</label>';\n");
        s.append("    h+='<input id=\"ef-mi-'+i+'\" value=\"'+escA(f.min)+'\" placeholder=\"ex: 0.01\"></div>';\n");
        s.append("    h+='<div class=\"fg\"><label>M\u00e1ximo</label>';\n");
        s.append("    h+='<input id=\"ef-ma-'+i+'\" value=\"'+escA(f.max)+'\" placeholder=\"ex: 9999\"></div>';\n");
        s.append("    h+='<div class=\"fg fg-full\"><label>Pattern (regex)</label>';\n");
        s.append("    h+='<input id=\"ef-pa-'+i+'\" value=\"'+escA(f.pattern)+'\" placeholder=\"ex: ^[A-Za-z]+$\"></div>';\n");
        s.append("    h+='<div class=\"fg fg-full\"><label>Mensagem de erro</label>';\n");
        s.append("    h+='<input id=\"ef-em-'+i+'\" value=\"'+escA(f.errorMsg)+'\" placeholder=\"ex: Formato inv\u00e1lido\"></div>';\n");
        s.append("    h+='<div class=\"fg fg-full\"><div class=\"toggle-row\">';\n");
        s.append("    h+='<input type=\"checkbox\" id=\"ef-rq-'+i+'\"'+(f.required?' checked':'')+'>';\n");
        s.append("    h+='<label for=\"ef-rq-'+i+'\" class=\"toggle-label\">Obrigat\u00f3rio</label></div></div>';\n");
        s.append("    h+='</div></div></div>';\n");
        s.append("  });\n");
        s.append("  h+='<div class=\"form-footer\">';\n");
        s.append("  h+='<button class=\"btn btn-primary\" onclick=\"doGenerateEntity()\">'+svgCode()+' Gerar arquivo .java</button>';\n");
        s.append("  h+='<span class=\"required-note\">* Campos obrigat\u00f3rios</span>';\n");
        s.append("  h+='</div></div></div>';\n");
        s.append("  ct.innerHTML=h;\n");
        s.append("}\n\n");

        s.append("async function doGenerateEntity(){\n");
        s.append("  ebSyncAll();\n");
        s.append("  var cls=(_ebCls||'').trim();\n");
        s.append("  if(!cls||!/^[A-Z][a-zA-Z0-9]*$/.test(cls)){\n");
        s.append("    toast('Nome da classe inv\u00e1lido. Use PascalCase, ex: MinhaEntidade','error');return;\n");
        s.append("  }\n");
        s.append("  var valid=_ebFields.filter(function(f){return f.name.trim()&&f.label.trim();});\n");
        s.append("  if(!valid.length){toast('Adicione ao menos um campo com nome e label preenchidos','error');return;}\n");
        s.append("  var badName=valid.find(function(f){return !/^[a-z][a-zA-Z0-9]*$/.test(f.name.trim());});\n");
        s.append("  if(badName){toast('Nome do campo \"'+badName.name+'\" inv\u00e1lido. Use camelCase','error');return;}\n");
        s.append("  try{\n");
        s.append("    var r=await req('/api/_tools/new-entity','POST',{className:cls,label:_ebLbl||cls,fields:valid});\n");
        s.append("    toast('Arquivo '+r.file+' gerado!','success');\n");
        s.append("    var info='<div class=\"card\" style=\"margin-top:16px\">';\n");
        s.append("    info+='<div class=\"card-head\"><span class=\"card-head-title\">Pr\u00f3ximos passos</span></div>';\n");
        s.append("    info+='<div class=\"card-body\" style=\"padding:20px 24px\">';\n");
        s.append("    info+='<ol style=\"margin:0;padding-left:20px;line-height:2.2;color:var(--g700);font-size:14px\">';\n");
        s.append("    info+='<li>Recompile: <code style=\"background:var(--g100);padding:1px 7px;border-radius:4px\">mvn compile</code></li>';\n");
        s.append("    info+='<li>Reinicie: <code style=\"background:var(--g100);padding:1px 7px;border-radius:4px\">mvn exec:java</code></li>';\n");
        s.append("    info+='<li>A entidade <strong>'+escH(cls)+'</strong> aparecer\u00e1 automaticamente no menu lateral.</li>';\n");
        s.append("    info+='</ol></div></div>';\n");
        s.append("    document.getElementById('content').insertAdjacentHTML('beforeend',info);\n");
        s.append("  }catch(err){toast('Erro: '+err.message,'error');}\n");
        s.append("}\n\n");

        s.append("function vField(el,jn,ei){\n");
        s.append("  var col=ENT[ei].columns.find(function(c){return c.javaName===jn;});\n");
        s.append("  if(!col)return true;\n");
        s.append("  var raw=el.type==='checkbox'?String(el.checked):el.value;\n");
        s.append("  var stripped=col.mask?stripMask(raw,resolveMask(col.mask)):raw;\n");
        s.append("  var v=stripped.trim();\n");
        s.append("  var err='';\n");
        s.append("  if(col.required&&v==='')err=col.errorMsg||(escH(col.label)+' &#233; obrigat&#243;rio');\n");
        s.append("  else if(v!==''&&col.minLength>0&&v.length<col.minLength)err=col.errorMsg||('M&#237;nimo de '+col.minLength+' caracteres');\n");
        s.append("  else if(v!==''&&col.maxLength>0&&v.length>col.maxLength)err=col.errorMsg||('M&#225;ximo de '+col.maxLength+' caracteres');\n");
        s.append("  else if(v!==''&&col.pattern&&!new RegExp(col.pattern).test(v))err=col.errorMsg||('Formato inv&#225;lido');\n");
        s.append("  else if(v!==''&&col.min!==''&&parseFloat(v)<parseFloat(col.min))err=col.errorMsg||('Valor m&#237;nimo: '+col.min);\n");
        s.append("  else if(v!==''&&col.max!==''&&parseFloat(v)>parseFloat(col.max))err=col.errorMsg||('Valor m&#225;ximo: '+col.max);\n");
        s.append("  var fe=document.getElementById('fe-'+jn);\n");
        s.append("  if(fe)fe.innerHTML=err?'<svg width=\"12\" height=\"12\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.5\" style=\"flex-shrink:0;margin-top:1px\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><line x1=\"12\" y1=\"8\" x2=\"12\" y2=\"12\"/><line x1=\"12\" y1=\"16\" x2=\"12.01\" y2=\"16\"/></svg>'+err:'';\n");
        s.append("  el.classList.toggle('err',!!err);\n");
        s.append("  el.classList.toggle('ok',!err&&v!=='');\n");
        s.append("  return !err;\n");
        s.append("}\n\n");

        s.append("async function doSubmit(ev,i,editId){\n");
        s.append("  ev.preventDefault();\n");
        s.append("  var e=ENT[i],form=document.getElementById('cf');\n");
        s.append("  var ok=true,bad=[];\n");
        s.append("  var data={};\n");
        s.append("  e.columns.forEach(function(col){\n");
        s.append("    var el=form.elements[col.javaName];\n");
        s.append("    if(!el)return;\n");
        s.append("    if(!vField(el,col.javaName,i)){ok=false;bad.push(col.label);}\n");
        s.append("    var raw=el.type==='checkbox'?String(el.checked):el.value;\n");
        s.append("    var stripped=col.mask?stripMask(raw,resolveMask(col.mask)):raw;\n");
        s.append("    data[col.javaName]=stripped===''?null:stripped;\n");
        s.append("  });\n");
        s.append("  var fb=document.getElementById('fb'),fbm=document.getElementById('fb-msg');\n");
        s.append("  if(!ok){\n");
        s.append("    fbm.innerHTML='Corrija os campos: <strong>'+escH(bad.join(', '))+'</strong>';\n");
        s.append("    fb.className='err-banner show';\n");
        s.append("    fb.scrollIntoView({behavior:'smooth',block:'nearest'});\n");
        s.append("    return;\n");
        s.append("  }\n");
        s.append("  fb.className='err-banner';\n");
        s.append("  var btn=document.getElementById('fsub');\n");
        s.append("  btn.disabled=true;\n");
        s.append("  btn.innerHTML='<div class=\"loader\" style=\"width:16px;height:16px;border-width:2px;margin:0\"></div> Salvando...';\n");
        s.append("  try{\n");
        s.append("    if(editId){await req(e.apiPath+'/'+editId,'PUT',data);toast('Registro atualizado!','success');}\n");
        s.append("    else{await req(e.apiPath,'POST',data);toast('Registro criado!','success');}\n");
        s.append("    go(i,'list');\n");
        s.append("  }catch(err){\n");
        s.append("    toast('Erro: '+err.message,'error');\n");
        s.append("    btn.disabled=false;\n");
        s.append("    btn.innerHTML=svgCheck()+(editId?' Atualizar':' Salvar');\n");
        s.append("  }\n");
        s.append("}\n\n");

        s.append("function openDtl(ei,rowId){\n");
        s.append("  var e=ENT[ei],row=_rows[rowId];\n");
        s.append("  if(!row)return;\n");
        s.append("  document.getElementById('dtl-h').textContent=e.label;\n");
        s.append("  document.getElementById('dtl-sub').textContent='Registro #'+row.id;\n");
        s.append("  var h='<div class=\"dtl-cell\"><div class=\"dtl-key\">ID</div><div class=\"dtl-val\"><span class=\"badge badge-id\">'+row.id+'</span></div></div>';\n");
        s.append("  e.columns.forEach(function(col){\n");
        s.append("    var v=gv(row,col);\n");
        s.append("    var dispV=(v===null||v===undefined||v==='')?\n");
        s.append("      '<span class=\"dtl-empty\">N&#227;o informado</span>':\n");
        s.append("      escH(String(v));\n");
        s.append("    h+='<div class=\"dtl-cell\">';\n");
        s.append("    h+='<div class=\"dtl-key\">'+escH(col.label);\n");
        s.append("    if(col.mask)h+=' <span class=\"mask-label\">'+escH(col.mask)+'</span>';\n");
        s.append("    h+='</div><div class=\"dtl-val\">'+dispV+'</div></div>';\n");
        s.append("  });\n");
        s.append("  var total=e.columns.length+1;\n");
        s.append("  if(total%2!==0)h+='<div class=\"dtl-cell\"></div>';\n");
        s.append("  document.getElementById('dtl-body').innerHTML=h;\n");
        s.append("  document.getElementById('dtl-edit').onclick=function(){closeOv('dtl-ov');go(ei,'form',row.id);};\n");
        s.append("  openOv('dtl-ov');\n");
        s.append("}\n\n");

        s.append("var _dei=-1,_did=-1;\n");
        s.append("function openDel(ei,id){\n");
        s.append("  _dei=ei;_did=id;\n");
        s.append("  document.getElementById('del-msg').textContent='Excluir registro #'+id+' de '+ENT[ei].label+'? Esta a\u00e7\u00e3o n\u00e3o pode ser desfeita.';\n");
        s.append("  document.getElementById('del-btn').onclick=doDel;\n");
        s.append("  openOv('del-ov');\n");
        s.append("}\n");
        s.append("async function doDel(){\n");
        s.append("  var btn=document.getElementById('del-btn');\n");
        s.append("  btn.disabled=true;btn.textContent='Excluindo...';\n");
        s.append("  try{\n");
        s.append("    await req(ENT[_dei].apiPath+'/'+_did,'DELETE');\n");
        s.append("    toast('Registro #'+_did+' exclu\\u00eddo','success');\n");
        s.append("    closeOv('del-ov');\n");
        s.append("    go(_dei,'list');\n");
        s.append("  }catch(err){toast('Erro: '+err.message,'error');}\n");
        s.append("  btn.disabled=false;btn.textContent='Excluir';\n");
        s.append("}\n\n");

        s.append("function openOv(id){document.getElementById(id).classList.add('open');}\n");
        s.append("function closeOv(id){document.getElementById(id).classList.remove('open');}\n");
        s.append("function handleOvClick(ev,id){if(ev.target===ev.currentTarget)closeOv(id);}\n\n");

        s.append("async function req(path,method,body){\n");
        s.append("  var opts={method:method||'GET',headers:{'Content-Type':'application/json'}};\n");
        s.append("  if(body)opts.body=JSON.stringify(body);\n");
        s.append("  var r=await fetch(path,opts);\n");
        s.append("  if(r.status===204)return null;\n");
        s.append("  var t=await r.text();\n");
        s.append("  var j=t?JSON.parse(t):null;\n");
        s.append("  if(!r.ok)throw new Error((j&&j.error)?j.error:r.statusText);\n");
        s.append("  return j;\n");
        s.append("}\n\n");

        s.append("function gv(row,col){var v=row[col.columnName];return v!==undefined?v:row[col.javaName];}\n");
        s.append("function escH(s){s=String(s||'');return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');}\n");
        s.append("function escA(s){s=String(s||'');return s.replace(/\"/g,'&quot;');}\n");
        s.append("function inferType(t){t=(t||'').toUpperCase();if(t==='DATE')return 'date';if(t==='TIMESTAMP')return 'datetime-local';return 'text';}\n\n");

        s.append("function toast(msg,type){\n");
        s.append("  var el=document.getElementById('toast');\n");
        s.append("  var bar=document.getElementById('t-bar');\n");
        s.append("  var lbl=document.getElementById('t-lbl');\n");
        s.append("  var m=document.getElementById('t-msg');\n");
        s.append("  type=type||'info';\n");
        s.append("  bar.className='t-bar '+type;\n");
        s.append("  lbl.textContent=type==='success'?'Sucesso':type==='error'?'Erro':'Info';\n");
        s.append("  m.textContent=msg;\n");
        s.append("  el.className='show';\n");
        s.append("  clearTimeout(el._t);\n");
        s.append("  el._t=setTimeout(function(){el.className='';},4000);\n");
        s.append("}\n\n");

        s.append("function svgList(){return '<svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><rect x=\"3\" y=\"3\" width=\"18\" height=\"18\" rx=\"2\"/><line x1=\"3\" y1=\"9\" x2=\"21\" y2=\"9\"/><line x1=\"3\" y1=\"15\" x2=\"21\" y2=\"15\"/><line x1=\"9\" y1=\"3\" x2=\"9\" y2=\"21\"/></svg>';}\n");
        s.append("function svgForm(){return '<svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7\"/><path d=\"M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4z\"/></svg>';}\n");
        s.append("function svgPlus(){return '<svg width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.5\"><line x1=\"12\" y1=\"5\" x2=\"12\" y2=\"19\"/><line x1=\"5\" y1=\"12\" x2=\"19\" y2=\"12\"/></svg>';}\n");
        s.append("function svgBack(){return '<svg width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><polyline points=\"15 18 9 12 15 6\"/></svg>';}\n");
        s.append("function svgEdit(){return '<svg width=\"13\" height=\"13\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7\"/><path d=\"M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4z\"/></svg>';}\n");
        s.append("function svgTrash(){return '<svg width=\"13\" height=\"13\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><polyline points=\"3 6 5 6 21 6\"/><path d=\"M19 6l-1 14H6L5 6\"/><path d=\"M10 11v6\"/><path d=\"M14 11v6\"/></svg>';}\n");
        s.append("function svgCheck(){return '<svg width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.5\"><polyline points=\"20 6 9 17 4 12\"/></svg>';}\n");
        s.append("function svgWarn(){return '<svg width=\"28\" height=\"28\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.5\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><line x1=\"12\" y1=\"8\" x2=\"12\" y2=\"12\"/><line x1=\"12\" y1=\"16\" x2=\"12.01\" y2=\"16\"/></svg>';}\n");
        s.append("function svgEmpty(){return '<svg width=\"28\" height=\"28\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.5\"><rect x=\"3\" y=\"3\" width=\"18\" height=\"18\" rx=\"2\"/><line x1=\"3\" y1=\"9\" x2=\"21\" y2=\"9\"/><line x1=\"3\" y1=\"15\" x2=\"21\" y2=\"15\"/><line x1=\"9\" y1=\"3\" x2=\"9\" y2=\"21\"/></svg>';}\n");
        s.append("function svgCube(){return '<svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z\"/><polyline points=\"3.27 6.96 12 12.01 20.73 6.96\"/><line x1=\"12\" y1=\"22.08\" x2=\"12\" y2=\"12\"/></svg>';}\n");
        s.append("function svgCode(){return '<svg width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.5\"><polyline points=\"16 18 22 12 16 6\"/><polyline points=\"8 6 2 12 8 18\"/></svg>';}\n\n");

        s.append("buildNav();\n");
        s.append("if(ENT.length>0)go(0,'list');\n");
    }

    private static String buildMetaJson(List<EntityMetadata> entities) {
        return "[" + entities.stream().map(meta -> {
            String cols = meta.getColumnsSorted().stream()
                .map(UiGenerator::colJson)
                .collect(Collectors.joining(","));
            return "{\"label\":\"" + ej(meta.getLabel()) + "\","
                 + "\"apiPath\":\"" + meta.getApiPath() + "\","
                 + "\"columns\":[" + cols + "]}";
        }).collect(Collectors.joining(",")) + "]";
    }

    private static String colJson(ColumnMetadata c) {
        return "{"
            + "\"javaName\":\""    + ej(c.javaName())    + "\","
            + "\"columnName\":\"" + ej(c.columnName())   + "\","
            + "\"label\":\""      + ej(c.label())        + "\","
            + "\"sqlType\":\""    + ej(c.sqlType())      + "\","
            + "\"required\":"     + c.required()         + ","
            + "\"minLength\":"    + c.minLength()        + ","
            + "\"maxLength\":"    + c.maxLength()        + ","
            + "\"min\":\""        + ej(c.min())          + "\","
            + "\"max\":\""        + ej(c.max())          + "\","
            + "\"pattern\":\""    + ej(c.pattern())      + "\","
            + "\"errorMsg\":\""   + ej(c.errorMsg())     + "\","
            + "\"placeholder\":\"" + ej(c.placeholder()) + "\","
            + "\"mask\":\""        + ej(c.mask())        + "\""
            + "}";
    }

    private static String ej(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }
}
