import com.rajasaur.dataloader.services.DynamicDomainService

// Place your Spring DSL code here
beans = {
    dynamicDomainService(com.rajasaur.dataloader.services.DynamicDomainService)
        renderEditor(com.rajasaur.dataloader.services.RenderEditor)
}
