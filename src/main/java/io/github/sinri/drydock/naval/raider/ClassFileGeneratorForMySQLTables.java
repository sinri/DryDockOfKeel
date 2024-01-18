package io.github.sinri.drydock.naval.raider;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.mysql.KeelMySQLDataSourceProvider;
import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.github.sinri.keel.mysql.NamedMySQLDataSource;
import io.github.sinri.keel.mysql.dev.TableRowClassSourceCodeGenerator;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.2.4
 */
abstract public class ClassFileGeneratorForMySQLTables extends Privateer {

    /**
     * @return MySQL表对应类所在根对应包的绝对路径，没有最后的斜杠。一般从配置`table.package.path`中读取。
     */
    protected String getTablePackagePath() {
        return Keel.config("table.package.path");
    }

    /**
     * @return MySQL表对应类所在根对应包的Namespace，没有最后的点。
     */
    abstract protected String getTablePackage();

    @Nullable
    abstract public String getStrictEnumPackage();

    @Nullable
    abstract public String getEnvelopePackage();

    public boolean isProvideConstSchema() {
        return false;
    }

    public boolean isProvideConstTable() {
        return false;
    }

    public boolean isProvideConstSchemaAndTable() {
        return true;
    }

    @Nonnull
    protected String buildPackageNameForSchema(@Nonnull String schemaName) {
        var x = schemaName.replaceAll("[^A-Za-z0-9]+", "").toLowerCase();
        if (x.isBlank()) throw new IllegalArgumentException("SCHEMA PACKAGE NAME EMPTY");
        return x;
    }

    protected <C extends NamedMySQLConnection> Future<Void> rebuildTablesInSchema(
            String dataSourceName,
            Function<SqlConnection, C> sqlConnectionWrapper,
            String schemaName
    ) {

        return rebuildTablesInSchema(dataSourceName, sqlConnectionWrapper, schemaName, buildPackageNameForSchema(schemaName), null);
    }

    protected <C extends NamedMySQLConnection> Future<Void> rebuildTablesInSchema(
            String dataSourceName,
            Function<SqlConnection, C> sqlConnectionWrapper,
            String schemaName,
            @Nullable List<String> tables
    ) {

        return rebuildTablesInSchema(dataSourceName, sqlConnectionWrapper, schemaName, buildPackageNameForSchema(schemaName), tables);
    }

    private <C extends NamedMySQLConnection> Future<Void> rebuildTablesInSchema(
            String dataSourceName,
            Function<SqlConnection, C> sqlConnectionWrapper,
            String schemaName,
            String schemaPackageName,
            @Nullable List<String> tables
    ) {
        NamedMySQLDataSource<C> mySQLDataSource = KeelMySQLDataSourceProvider.initializeNamedMySQLDataSource(
                dataSourceName,
                sqlConnectionWrapper
        );

        var dir = getTablePackagePath() + "/" + dataSourceName + "/" + schemaPackageName;
        return Keel.getVertx().fileSystem().readDir(dir)
                .compose(files -> {
                    return KeelAsyncKit.iterativelyCall(files, file -> {
                        if (file.endsWith("/package-info.java")) {
                            return Future.succeededFuture();
                        } else {
                            return Keel.getVertx().fileSystem().delete(file);
                        }
                    });
                })
                .compose(v -> {
                    return mySQLDataSource.withConnection(sqlConnection -> {
                        var x = new TableRowClassSourceCodeGenerator(sqlConnection)
                                .forSchema(schemaName);
                        if (tables != null) {
                            x.forTables(tables);
                        }
                        String strictEnumPackage = getStrictEnumPackage();
                        if (strictEnumPackage != null) {
                            x.setStrictEnumPackage(strictEnumPackage);
                        }
                        String envelopePackage = getEnvelopePackage();
                        if (envelopePackage != null) {
                            x.setEnvelopePackage(envelopePackage);
                        }
                        return x
                                .setProvideConstSchema(isProvideConstSchema())
                                .setProvideConstTable(isProvideConstTable())
                                .setProvideConstSchemaAndTable(isProvideConstSchemaAndTable())
                                .generate(getTablePackage() + "." + dataSourceName + "." + schemaPackageName, dir);
                    });
                })
                .compose(v -> {
                    return Future.succeededFuture();
                });
    }
}
