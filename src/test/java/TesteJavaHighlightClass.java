import dev.cwby.Deditor;
import dev.cwby.commands.ICommand;
import dev.cwby.editor.FileChunkLoader;
import dev.cwby.editor.TextBuffer;

import java.io.File;

public class TesteJavaHighlightClass implements ICommand {


    @Override
    public boolean run(String[] args) {
        if (args.length >= 2) {
            System.out.println(args[1]);
            File file = new File(args[1]);
            if (file.exists()) {
                TextBuffer textBuffer = new TextBuffer(new FileChunkLoader(file, 10240));
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
