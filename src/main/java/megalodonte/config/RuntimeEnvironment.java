package megalodonte.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Runtime-only configuration: process environment first, then a private per-app .env. */
public final class RuntimeEnvironment {
    private final Path file;
    private final Map<String,String> environment;

    public RuntimeEnvironment(Path file) { this(file,System.getenv()); }
    public RuntimeEnvironment(Path file,Map<String,String> environment) {
        this.file=file.toAbsolutePath().normalize();
        this.environment=Map.copyOf(environment);
    }

    public static RuntimeEnvironment forApp(String app) {
        if(app==null || !app.matches("[a-z0-9][a-z0-9-]*"))
            throw new IllegalArgumentException("Nome de aplicativo inválido.");
        String appData=System.getenv("APPDATA");
        String xdg=System.getenv("XDG_CONFIG_HOME");
        Path base=appData!=null && !appData.isBlank() ? Path.of(appData,"Megalodonte")
                : xdg!=null && !xdg.isBlank() ? Path.of(xdg,"megalodonte")
                : Path.of(System.getProperty("user.home"),".config","megalodonte");
        return new RuntimeEnvironment(base.resolve(app).resolve(".env"));
    }

    /** Development fallback; never loads an arbitrary working-directory .env. */
    public static RuntimeEnvironment forAppOrProject(String app) {
        RuntimeEnvironment user=forApp(app);
        if(Files.isRegularFile(user.path())) return user;
        Path project=Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if(Files.isRegularFile(project.resolve("packaging.toml")) && Files.isRegularFile(project.resolve(".env")))
            return new RuntimeEnvironment(project.resolve(".env"));
        return user;
    }

    public Path path() { return file; }

    public String get(String key) throws IOException {
        if(key==null || !key.matches("[A-Za-z_][A-Za-z0-9_]*"))
            throw new IllegalArgumentException("Nome de configuração inválido.");
        String process=environment.get(key);
        if(process!=null && !process.isBlank()) return process;
        return values().getOrDefault(key,"");
    }

    public String require(String key) throws IOException {
        String value=get(key);
        if(value.isBlank()) throw new IOException("Configure "+key+" em "+file+" ou no ambiente.");
        return value;
    }

    private Map<String,String> values() throws IOException {
        var result=new HashMap<String,String>();
        if(!Files.isRegularFile(file)) return result;
        int number=0;
        for(String raw:Files.readAllLines(file,StandardCharsets.UTF_8)) {
            number++;
            String line=raw.strip();
            if(line.isEmpty() || line.startsWith("#")) continue;
            if(line.startsWith("export ")) line=line.substring(7).strip();
            int separator=line.indexOf('=');
            if(separator<1) throw new IOException("Linha inválida no .env: "+number);
            String key=line.substring(0,separator).strip();
            if(!key.matches("[A-Za-z_][A-Za-z0-9_]*")) throw new IOException("Chave inválida no .env: "+number);
            String value=line.substring(separator+1).strip();
            if(value.length()>=2 && ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'")))) value=value.substring(1,value.length()-1);
            result.put(key,value);
        }
        return result;
    }
}
