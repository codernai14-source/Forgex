<template>
  <div class="system-config">
    <a-card :bordered="false" :loading="loading">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="system" :tab="t('system.config.tabSystem')">
          <a-form
            :model="basicConfig"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 16 }"
            layout="horizontal"
          >
            <a-form-item :label="t('system.config.systemName')" name="systemName">
              <a-input
                v-model:value="basicConfig.systemName"
                :placeholder="t('system.config.systemNamePlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.systemLogo')" name="systemLogo">
              <div class="system-logo-upload">
                <AvatarUpload
                  v-model="basicConfig.systemLogo"
                  module-code="sys_config_logo"
                  module-name="系统配置Logo"
                  @success="handleLogoUploadSuccess"
                />
              </div>
            </a-form-item>

            <a-form-item :label="t('system.config.systemVersion')" name="systemVersion">
              <a-input
                v-model:value="basicConfig.systemVersion"
                :placeholder="t('system.config.systemVersionPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.copyright')" name="copyright">
              <a-input
                v-model:value="basicConfig.copyright"
                :placeholder="t('system.config.copyrightPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.copyrightLink')" name="copyrightLink">
              <a-input
                v-model:value="basicConfig.copyrightLink"
                :placeholder="t('system.config.copyrightLinkPlaceholder')"
              />
            </a-form-item>

            <a-form-item :wrapper-col="{ span: 24 }">
              <a-space>
                <a-button data-guide-id="sys-config-save" type="primary" :loading="savingSystem" @click="saveSystemConfig">
                  {{ t('common.save') }}
                </a-button>
                <a-button data-guide-id="sys-config-reset" @click="resetSystemConfig">{{ t('common.reset') }}</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="portal" :tab="t('system.config.tabPortal')">
          <a-form
            :model="basicConfig"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 16 }"
            layout="horizontal"
          >
            <a-form-item :label="t('system.config.loginTitle')" name="loginPageTitle">
              <a-input
                v-model:value="basicConfig.loginPageTitle"
                :placeholder="t('system.config.loginTitlePlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.loginSubtitle')" name="loginPageSubtitle">
              <a-input
                v-model:value="basicConfig.loginPageSubtitle"
                :placeholder="t('system.config.loginSubtitlePlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.loginLayout')" name="loginLayout">
              <a-radio-group v-model:value="basicConfig.loginLayout">
                <a-radio value="center">{{ t('system.config.layoutCenter') }}</a-radio>
                <a-radio value="split">{{ t('system.config.layoutSplit') }}</a-radio>
                <a-radio value="compact">{{ t('system.config.layoutCompact') }}</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item :label="t('system.config.loginStyle')" name="loginStyle">
              <a-radio-group v-model:value="basicConfig.loginStyle">
                <a-radio value="cyber">{{ t('system.config.styleCyber') }}</a-radio>
                <a-radio value="simple">{{ t('system.config.styleSimple') }}</a-radio>
                <a-radio value="classic">{{ t('system.config.styleClassic') }}</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item :label="t('system.config.backgroundType')" name="loginBackgroundType">
              <a-radio-group v-model:value="basicConfig.loginBackgroundType">
                <a-radio value="video">{{ t('system.config.bgVideo') }}</a-radio>
                <a-radio value="image">{{ t('system.config.bgImage') }}</a-radio>
                <a-radio value="color">{{ t('system.config.bgColor') }}</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item
              v-if="basicConfig.loginBackgroundType === 'video'"
              :label="t('system.config.bgVideo')"
              name="loginBackgroundVideo"
            >
              <a-upload
                v-model:file-list="videoFileList"
                :before-upload="handleVideoBeforeUpload"
                :custom-request="handleVideoUpload"
                :show-upload-list="false"
                accept="video/*"
              >
                <a-space>
                  <a-button type="primary" :loading="videoUploading">
                    <UploadOutlined />
                    {{ videoUploading ? t('common.uploading') : t('common.uploadVideo') }}
                  </a-button>
                  <a-button v-if="basicConfig.loginBackgroundVideo" @click="handleVideoRemove">
                    <DeleteOutlined />
                    {{ t('common.remove') }}
                  </a-button>
                </a-space>
              </a-upload>
              <div v-if="basicConfig.loginBackgroundVideo" class="media-preview">
                <video
                  :src="formatMediaUrl(basicConfig.loginBackgroundVideo)"
                  controls
                  class="video-preview"
                ></video>
              </div>
            </a-form-item>

            <a-form-item
              v-if="basicConfig.loginBackgroundType === 'image'"
              :label="t('system.config.bgImage')"
              name="loginBackgroundImage"
            >
              <a-upload
                v-model:file-list="bgImageFileList"
                :before-upload="handleBgImageBeforeUpload"
                :custom-request="handleBgImageUpload"
                :show-upload-list="false"
                accept="image/*"
              >
                <a-space>
                  <a-avatar
                    v-if="basicConfig.loginBackgroundImage"
                    :size="120"
                    :src="formatMediaUrl(basicConfig.loginBackgroundImage)"
                  >
                    <template #icon>
                      <PictureOutlined />
                    </template>
                  </a-avatar>
                  <a-button type="primary" :loading="bgImageUploading">
                    <UploadOutlined />
                    {{ bgImageUploading ? t('common.uploading') : t('common.uploadImage') }}
                  </a-button>
                  <a-button v-if="basicConfig.loginBackgroundImage" @click="handleBgImageRemove">
                    <DeleteOutlined />
                    {{ t('common.remove') }}
                  </a-button>
                </a-space>
              </a-upload>
            </a-form-item>

            <a-form-item
              v-if="basicConfig.loginBackgroundType === 'color'"
              :label="t('system.config.bgColor')"
              name="loginBackgroundColor"
            >
              <a-space>
                <a-input
                  v-model:value="basicConfig.loginBackgroundColor"
                  :placeholder="t('system.config.bgColorPlaceholder')"
                />
                <a-input type="color" v-model:value="basicConfig.loginBackgroundColor" class="color-input" />
              </a-space>
            </a-form-item>

            <a-form-item :label="t('system.config.showOAuth')" name="showOAuthLogin">
              <a-switch v-model:checked="basicConfig.showOAuthLogin" />
            </a-form-item>

            <a-form-item :label="t('system.config.showRegisterEntry')" name="showRegisterEntry">
              <a-switch v-model:checked="basicConfig.showRegisterEntry" />
            </a-form-item>

            <a-form-item :label="t('system.config.registerUrl')" name="registerUrl">
              <a-input
                v-model:value="basicConfig.registerUrl"
                :placeholder="t('system.config.registerUrlPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.primaryColor')" name="primaryColor">
              <a-input
                v-model:value="basicConfig.primaryColor"
                :placeholder="t('system.config.primaryColorPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.secondaryColor')" name="secondaryColor">
              <a-input
                v-model:value="basicConfig.secondaryColor"
                :placeholder="t('system.config.secondaryColorPlaceholder')"
              />
            </a-form-item>

            <a-form-item :wrapper-col="{ span: 24 }">
              <a-space>
                <a-button type="primary" :loading="savingPortal" @click="savePortalConfig">
                  {{ t('common.save') }}
                </a-button>
                <a-button @click="resetPortalConfig">{{ t('common.reset') }}</a-button>
                <a-button @click="openPreview">{{ t('common.preview') }}</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="security" :tab="t('system.config.tabSecurity')">
          <a-alert
            class="session-timeout-alert"
            type="info"
            show-icon
            :message="t('system.config.sessionTimeoutNoticeTitle')"
            :description="t('system.config.sessionTimeoutNoticeDesc')"
          />
          <div class="sidebar-config-layout">
            <div class="sidebar-nav-list">
              <button
                v-for="item in securityNavOptions"
                :key="item.value"
                type="button"
                class="sidebar-nav-card"
                :class="{ 'sidebar-nav-card--active': securitySubTab === item.value }"
                @click="securitySubTab = item.value"
              >
                <span class="sidebar-nav-card__title">{{ t(item.titleKey) }}</span>
                <span class="sidebar-nav-card__desc">{{ t(item.descKey) }}</span>
              </button>
            </div>

            <div class="sidebar-config-main">
              <div class="sidebar-config-banner">
                <div class="sidebar-config-banner__label">{{ t(currentSecurityNav.titleKey) }}</div>
                <div class="sidebar-config-banner__text">{{ t(currentSecurityNav.descKey) }}</div>
              </div>

              <!-- 验证码配置 -->
              <a-form v-if="securitySubTab === 'captcha'" :model="securityConfig" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" layout="horizontal">
                <a-form-item :label="t('system.config.captchaMode')" name="captcha.mode">
                  <a-radio-group v-model:value="securityConfig.captcha.mode">
                    <a-radio value="none">{{ t('system.config.captchaNone') }}</a-radio>
                    <a-radio value="image">{{ t('system.config.captchaImage') }}</a-radio>
                    <a-radio value="slider">{{ t('system.config.captchaSlider') }}</a-radio>
                  </a-radio-group>
                </a-form-item>

                <template v-if="securityConfig.captcha.mode === 'image'">
                  <a-form-item :label="t('system.config.imageKeyPrefix')" name="captcha.image.keyPrefix">
                    <a-input v-model:value="securityConfig.captcha.image.keyPrefix" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.imageExpireSeconds')" name="captcha.image.expireSeconds">
                    <a-input-number v-model:value="securityConfig.captcha.image.expireSeconds" :min="30" :max="600" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.imageWidth')" name="captcha.image.width">
                    <a-input-number v-model:value="securityConfig.captcha.image.width" :min="80" :max="300" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.imageHeight')" name="captcha.image.height">
                    <a-input-number v-model:value="securityConfig.captcha.image.height" :min="30" :max="120" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.imageLength')" name="captcha.image.length">
                    <a-input-number v-model:value="securityConfig.captcha.image.length" :min="3" :max="8" />
                  </a-form-item>
                </template>

                <template v-if="securityConfig.captcha.mode === 'slider'">
                  <a-form-item :label="t('system.config.sliderKeyPrefix')" name="captcha.slider.keyPrefix">
                    <a-input v-model:value="securityConfig.captcha.slider.keyPrefix" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.sliderSecondaryKeyPrefix')" name="captcha.slider.secondaryKeyPrefix">
                    <a-input v-model:value="securityConfig.captcha.slider.secondaryKeyPrefix" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.sliderTokenExpireSeconds')" name="captcha.slider.tokenExpireSeconds">
                    <a-input-number v-model:value="securityConfig.captcha.slider.tokenExpireSeconds" :min="30" :max="600" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.sliderProvider')" name="captcha.slider.provider">
                    <a-input v-model:value="securityConfig.captcha.slider.provider" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.sliderSecondaryEnabled')" name="captcha.slider.secondaryEnabled">
                    <a-switch v-model:checked="securityConfig.captcha.slider.secondaryEnabled" />
                  </a-form-item>
                </template>

                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingSecurity" @click="saveSecurityConfig">{{ t('common.save') }}</a-button>
                    <a-button @click="resetSecurityConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <!-- 密码策略 -->
              <a-form v-if="securitySubTab === 'password'" :model="securityConfig" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" layout="horizontal">
                <a-form-item :label="t('system.config.passwordStore')" name="passwordPolicy.store">
                  <a-select v-model:value="securityConfig.passwordPolicy.store">
                    <a-select-option value="bcrypt">BCrypt（推荐，不可逆哈希）</a-select-option>
                    <a-select-option value="argon2">Argon2（高安全哈希）</a-select-option>
                    <a-select-option value="scrypt">SCrypt</a-select-option>
                    <a-select-option value="pbkdf2">PBKDF2</a-select-option>
                    <a-select-option value="sm4">SM4（国密对称加密）</a-select-option>
                    <a-select-option value="aes">AES-256-GCM（对称加密）</a-select-option>
                    <a-select-option value="sm2">SM2（国密非对称加密）</a-select-option>
                    <a-select-option value="rsa">RSA-2048（非对称加密）</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item :label="t('system.config.defaultPassword')" name="passwordPolicy.defaultPassword">
                  <a-input-password v-model:value="securityConfig.passwordPolicy.defaultPassword" :placeholder="t('system.config.defaultPasswordPlaceholder')" />
                </a-form-item>
                <a-form-item :label="t('system.config.passwordMinLength')" name="passwordPolicy.minLength">
                  <a-input-number v-model:value="securityConfig.passwordPolicy.minLength" :min="6" :max="32" />
                </a-form-item>
                <a-form-item :label="t('system.config.passwordRequireNumbers')" name="passwordPolicy.requireNumbers">
                  <a-switch v-model:checked="securityConfig.passwordPolicy.requireNumbers" />
                </a-form-item>
                <a-form-item :label="t('system.config.passwordRequireUppercase')" name="passwordPolicy.requireUppercase">
                  <a-switch v-model:checked="securityConfig.passwordPolicy.requireUppercase" />
                </a-form-item>
                <a-form-item :label="t('system.config.passwordRequireLowercase')" name="passwordPolicy.requireLowercase">
                  <a-switch v-model:checked="securityConfig.passwordPolicy.requireLowercase" />
                </a-form-item>
                <a-form-item :label="t('system.config.passwordRequireSymbols')" name="passwordPolicy.requireSymbols">
                  <a-switch v-model:checked="securityConfig.passwordPolicy.requireSymbols" />
                </a-form-item>
                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingSecurity" @click="saveSecurityConfig">{{ t('common.save') }}</a-button>
                    <a-button @click="resetSecurityConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <!-- 登录失败策略 -->
              <a-form v-if="securitySubTab === 'loginFailure'" :model="securityConfig" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" layout="horizontal">
                <a-form-item :label="t('system.config.failWindowMinutes')" name="loginSecurity.failWindowMinutes">
                  <a-input-number v-model:value="securityConfig.loginSecurity.failWindowMinutes" :min="1" :max="1440" :style="{ width: '100%' }" />
                </a-form-item>
                <a-form-item :label="t('system.config.maxFailCount')" name="loginSecurity.maxFailCount">
                  <a-input-number v-model:value="securityConfig.loginSecurity.maxFailCount" :min="1" :max="20" :style="{ width: '100%' }" />
                </a-form-item>
                <a-form-item :label="t('system.config.lockMinutes')" name="loginSecurity.lockMinutes">
                  <a-input-number v-model:value="securityConfig.loginSecurity.lockMinutes" :min="1" :max="1440" :style="{ width: '100%' }" />
                </a-form-item>
                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingSecurity" @click="saveSecurityConfig">{{ t('common.save') }}</a-button>
                    <a-button @click="resetSecurityConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <!-- 传输加密 -->
              <a-form v-if="securitySubTab === 'transport'" :model="securityConfig" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" layout="horizontal">
                <a-form-item :label="t('system.config.transportAlgorithm')" name="cryptoTransport.algorithm">
                  <a-select v-model:value="securityConfig.cryptoTransport.algorithm">
                    <a-select-option value="SM2">SM2（国密非对称加密）</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item :label="t('system.config.transportCipher')" name="cryptoTransport.cipher">
                  <a-select v-model:value="securityConfig.cryptoTransport.cipher">
                    <a-select-option value="BCD">BCD</a-select-option>
                    <a-select-option value="Hex">Hex</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item :label="t('system.config.transportPublicKey')" name="cryptoTransport.publicKey">
                  <a-textarea v-model:value="securityConfig.cryptoTransport.publicKey" :rows="3" :placeholder="t('system.config.transportPublicKeyPlaceholder')" />
                </a-form-item>
                <a-form-item :label="t('system.config.transportPrivateKey')" name="cryptoTransport.privateKey">
                  <a-textarea v-model:value="securityConfig.cryptoTransport.privateKey" :rows="3" :placeholder="t('system.config.transportPrivateKeyPlaceholder')" />
                </a-form-item>
                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingSecurity" @click="saveSecurityConfig">{{ t('common.save') }}</a-button>
                    <a-button @click="resetSecurityConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="personalHomepage" tab="个人首页">
          <div class="homepage-config-layout">
            <div class="homepage-module-list">
              <button
                v-for="moduleCard in homepageModuleCards"
                :key="moduleCard.value"
                type="button"
                class="homepage-module-card"
                :class="{ 'homepage-module-card--active': activeHomepageModule === moduleCard.value }"
                @click="activeHomepageModule = moduleCard.value"
              >
                <component :is="moduleCard.icon" class="homepage-module-card__icon" />
                <span class="homepage-module-card__body">
                  <span class="homepage-module-card__title">{{ moduleCard.title }}</span>
                  <span class="homepage-module-card__desc">{{ moduleCard.desc }}</span>
                </span>
              </button>
            </div>
            <div class="homepage-config-main">
              <PersonalHomepageDesigner
                v-if="activeHomepageModule === 'personal'"
                mode="manage"
                title="个人首页默认配置"
                description="维护公共级和当前租户级的默认布局，所有用户都能访问个人首页，并按这里作为初始门户。"
                :show-scope-selector="true"
              />
              <ModuleHomepageDesigner
                v-else
                :key="activeHomepageModule"
                :module-code="activeHomepageModule"
                mode="manage"
                :title="activeHomepageModuleCard.title"
                :description="activeHomepageModuleCard.desc"
                :show-scope-selector="true"
                :initial-edit-mode="true"
              />
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="email" :tab="t('system.config.tabEmail')">
          <div class="email-config-layout">
            <div class="email-provider-list">
              <button
                v-for="provider in emailProviderOptions"
                :key="provider.value"
                type="button"
                class="email-provider-card"
                :class="{ 'email-provider-card--active': emailConfig.providerType === provider.value }"
                @click="handleEmailProviderChange(provider.value)"
              >
                <span class="email-provider-card__title">{{ t(provider.titleKey) }}</span>
                <span class="email-provider-card__desc">{{ t(provider.descKey) }}</span>
              </button>
            </div>

            <div class="email-config-main">
              <div class="email-config-banner">
                <div class="email-config-banner__label">{{ t('system.config.emailProvider') }}</div>
                <div class="email-config-banner__text">{{ t('system.config.emailProviderHint') }}</div>
              </div>

              <a-form
                :model="emailConfig"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
                layout="horizontal"
              >
                <a-form-item :label="t('system.config.senderAccount')" name="senderAccount">
                  <a-input
                    v-model:value="emailConfig.senderAccount"
                    :placeholder="t('system.config.senderAccountPlaceholder')"
                  />
                </a-form-item>

                <a-form-item :label="t('system.config.senderPassword')" name="senderPassword">
                  <a-input-password
                    v-model:value="emailConfig.senderPassword"
                    :placeholder="t('system.config.senderPasswordPlaceholder')"
                  />
                </a-form-item>

                <a-form-item :label="t('system.config.smtpHost')" name="smtpHost">
                  <a-input
                    v-model:value="emailConfig.smtpHost"
                    :placeholder="t('system.config.smtpHostPlaceholder')"
                  />
                </a-form-item>

                <a-form-item :label="t('system.config.smtpPort')" name="smtpPort">
                  <a-input-number
                    v-model:value="emailConfig.smtpPort"
                    :min="1"
                    :max="65535"
                    :style="{ width: '100%' }"
                  />
                </a-form-item>

                <a-form-item :label="t('system.config.authEnabled')" name="authEnabled">
                  <a-switch v-model:checked="emailConfig.authEnabled" />
                </a-form-item>

                <a-form-item :label="t('system.config.sslEnabled')" name="sslEnabled">
                  <a-switch v-model:checked="emailConfig.sslEnabled" />
                </a-form-item>

                <a-form-item :label="t('system.config.starttlsEnabled')" name="starttlsEnabled">
                  <a-switch v-model:checked="emailConfig.starttlsEnabled" />
                </a-form-item>

                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingEmail" @click="saveEmail">
                      {{ t('common.save') }}
                    </a-button>
                    <a-button @click="resetEmailConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="upload" :tab="t('system.config.tabUpload')">
          <div class="sidebar-config-layout">
            <div class="sidebar-nav-list">
              <button
                v-for="item in uploadNavOptions"
                :key="item.value"
                type="button"
                class="sidebar-nav-card"
                :class="{ 'sidebar-nav-card--active': fileUploadConfig.storageType === item.value }"
                @click="handleUploadTypeChange(item.value)"
              >
                <span class="sidebar-nav-card__heading">
                  <component :is="item.icon" class="sidebar-nav-card__icon" />
                  <span class="sidebar-nav-card__title">{{ t(item.titleKey) }}</span>
                </span>
                <span class="sidebar-nav-card__desc">{{ t(item.descKey) }}</span>
              </button>
            </div>

            <div class="sidebar-config-main">
              <div class="sidebar-config-banner">
                <div class="sidebar-config-banner__label">{{ t(currentUploadNav.titleKey) }}</div>
                <div class="sidebar-config-banner__text">{{ t(currentUploadNav.descKey) }}</div>
              </div>

              <a-form
                :model="fileUploadConfig"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
                layout="horizontal"
              >
                <template v-if="fileUploadConfig.storageType === 'LOCAL'">
                  <a-form-item :label="t('system.config.localUploadPath')" name="localUploadPath">
                    <a-input-group compact>
                      <a-input
                        v-model:value="fileUploadConfig.localUploadPath"
                        readonly
                        style="width: calc(100% - 120px)"
                        :placeholder="t('system.config.localUploadPathPlaceholder')"
                      />
                      <a-button @click="openFolderPicker">{{ t('system.config.selectFolder') }}</a-button>
                    </a-input-group>
                    <div class="form-item-hint">
                      {{ t('system.config.localUploadPathHint') }}
                    </div>
                  </a-form-item>

                  <a-form-item :label="t('system.config.publicBaseUrl')" name="publicBaseUrl">
                    <a-input
                      v-model:value="fileUploadConfig.publicBaseUrl"
                      :placeholder="runtimeDefaults?.recommendedPublicBaseUrl || t('system.config.publicBaseUrlPlaceholder')"
                    />
                    <div class="form-item-hint">
                      {{ t('system.config.publicBaseUrlHint') }}
                      <a-button
                        v-if="runtimeDefaults?.recommendedPublicBaseUrl"
                        type="link"
                        size="small"
                        class="inline-link-button"
                        @click="applyRecommendedPublicBaseUrl"
                      >
                        {{ t('system.config.applyRecommendedUrl') }}
                      </a-button>
                    </div>
                  </a-form-item>

                  <a-form-item :label="t('system.config.accessPrefix')" name="accessPrefix">
                    <a-input
                      v-model:value="fileUploadConfig.accessPrefix"
                      :placeholder="t('system.config.accessPrefixPlaceholder')"
                    />
                  </a-form-item>

                  <a-alert
                    class="upload-url-preview"
                    type="info"
                    show-icon
                    :message="t('system.config.finalAccessExample')"
                    :description="fileUploadPreviewUrl"
                  />
                </template>

                <template v-else-if="fileUploadConfig.storageType === 'MINIO'">
                  <a-form-item :label="t('system.config.providerEndpoint')" name="minio.endpoint">
                    <a-input v-model:value="minioConfig.endpoint" placeholder="http://127.0.0.1:9000" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.providerBucketName')" name="minio.bucketName">
                    <a-input v-model:value="minioConfig.bucketName" placeholder="forgex" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.providerAccessKey')" name="minio.accessKey">
                    <a-input v-model:value="minioConfig.accessKey" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.providerSecretKey')" name="minio.secretKey">
                    <a-input-password v-model:value="minioConfig.secretKey" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.providerDomain')" name="minio.domain">
                    <a-input v-model:value="minioConfig.domain" placeholder="http://127.0.0.1:9000" />
                  </a-form-item>
                </template>

                <template v-else>
                  <a-form-item :label="t('system.config.providerEndpoint')" name="oss.endpoint">
                    <a-input v-model:value="ossConfig.endpoint" placeholder="oss-cn-hangzhou.aliyuncs.com" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.providerBucketName')" name="oss.bucketName">
                    <a-input v-model:value="ossConfig.bucketName" placeholder="forgex-files" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.providerAccessKeyId')" name="oss.accessKeyId">
                    <a-input v-model:value="ossConfig.accessKeyId" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.providerAccessKeySecret')" name="oss.accessKeySecret">
                    <a-input-password v-model:value="ossConfig.accessKeySecret" />
                  </a-form-item>
                  <a-form-item :label="t('system.config.providerDomain')" name="oss.domain">
                    <a-input v-model:value="ossConfig.domain" placeholder="https://cdn.example.com" />
                  </a-form-item>
                </template>

                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingUpload" @click="saveFileUploadConfig">
                      {{ t('common.save') }}
                    </a-button>
                    <a-button @click="resetFileUploadConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="crypto" :tab="t('system.config.tabCrypto')">
          <div class="sidebar-config-layout">
            <div class="sidebar-nav-list">
              <button
                v-for="item in cryptoNavOptions"
                :key="item.value"
                type="button"
                class="sidebar-nav-card"
                :class="{ 'sidebar-nav-card--active': cryptoSubTab === item.value }"
                @click="cryptoSubTab = item.value"
              >
                <span class="sidebar-nav-card__title">{{ t(item.titleKey) }}</span>
                <span class="sidebar-nav-card__desc">{{ t(item.descKey) }}</span>
              </button>
            </div>

            <div class="sidebar-config-main">
              <div class="sidebar-config-banner">
                <div class="sidebar-config-banner__label">{{ t(currentCryptoNav.titleKey) }}</div>
                <div class="sidebar-config-banner__text">{{ t(currentCryptoNav.descKey) }}</div>
              </div>

              <!-- 对称加密密钥 -->
              <a-form v-if="cryptoSubTab === 'symmetric'" :model="cryptoConfig" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" layout="horizontal">
                <a-alert type="warning" :message="t('system.config.keyChangeWarning')" show-icon style="margin-bottom: 16px" />
                <a-form-item :label="t('system.config.sm4Key')" name="sm4.keyHex">
                  <a-input-group compact>
                    <a-input-password v-model:value="cryptoConfig.sm4.keyHex" style="width: calc(100% - 120px)" :placeholder="t('system.config.sm4KeyPlaceholder')" />
                    <a-button type="primary" @click="handleGenerateSm4Key">{{ t('system.config.generateKey') }}</a-button>
                  </a-input-group>
                  <div class="form-item-hint">128位（32字符Hex）</div>
                </a-form-item>
                <a-form-item :label="t('system.config.aesKey')" name="aes.keyHex">
                  <a-input-group compact>
                    <a-input-password v-model:value="cryptoConfig.aes.keyHex" style="width: calc(100% - 120px)" :placeholder="t('system.config.aesKeyPlaceholder')" />
                    <a-button type="primary" @click="handleGenerateAesKey">{{ t('system.config.generateKey') }}</a-button>
                  </a-input-group>
                  <div class="form-item-hint">256位（64字符Hex）</div>
                </a-form-item>
                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingCrypto" @click="saveCryptoConfig">{{ t('common.save') }}</a-button>
                    <a-button @click="resetCryptoConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <!-- 非对称加密密钥 -->
              <a-form v-if="cryptoSubTab === 'asymmetric'" :model="cryptoConfig" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" layout="horizontal">
                <a-form-item :label="t('system.config.rsaKeySize')" name="rsa.keySize">
                  <a-select v-model:value="cryptoConfig.rsa.keySize">
                    <a-select-option :value="2048">RSA-2048（推荐）</a-select-option>
                    <a-select-option :value="4096">RSA-4096（高安全）</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item :label="t('system.config.rsaPublicKey')" name="rsa.publicKey">
                  <a-textarea v-model:value="cryptoConfig.rsa.publicKey" :rows="3" readonly :placeholder="t('system.config.rsaPublicKeyPlaceholder')" />
                </a-form-item>
                <a-form-item :label="t('system.config.rsaPrivateKey')" name="rsa.privateKey">
                  <a-textarea v-model:value="cryptoConfig.rsa.privateKey" :rows="3" :placeholder="t('system.config.rsaPrivateKeyPlaceholder')" />
                </a-form-item>
                <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
                  <a-button type="primary" @click="handleGenerateRsaKeyPair">
                    <KeyOutlined /> {{ t('system.config.generateRsaKeyPair') }}
                  </a-button>
                  <span class="form-item-hint" style="margin-left: 12px">{{ t('system.config.sm2InSecurityTab') }}</span>
                </a-form-item>
                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingCrypto" @click="saveCryptoConfig">{{ t('common.save') }}</a-button>
                    <a-button @click="resetCryptoConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <!-- 数据加密 -->
              <a-form v-if="cryptoSubTab === 'dataEncrypt'" :model="cryptoConfig" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" layout="horizontal">
                <a-divider orientation="left"><FileProtectOutlined /> {{ t('system.config.fileEncrypt') }}</a-divider>
                <a-form-item :label="t('system.config.fileEncryptEnabled')" name="fileEncrypt.enabled">
                  <a-switch v-model:checked="cryptoConfig.fileEncrypt.enabled" />
                </a-form-item>
                <a-form-item :label="t('system.config.fileEncryptAlgorithm')" name="fileEncrypt.defaultAlgorithm">
                  <a-select v-model:value="cryptoConfig.fileEncrypt.defaultAlgorithm" :disabled="!cryptoConfig.fileEncrypt.enabled">
                    <a-select-option value="aes">AES-256-GCM（国际标准，推荐）</a-select-option>
                    <a-select-option value="sm4">SM4-CBC（国密标准）</a-select-option>
                  </a-select>
                </a-form-item>
                <a-divider orientation="left"><DatabaseOutlined /> {{ t('system.config.fieldEncrypt') }}</a-divider>
                <a-form-item :label="t('system.config.fieldEncryptEnabled')" name="fieldEncrypt.enabled">
                  <a-switch v-model:checked="cryptoConfig.fieldEncrypt.enabled" />
                </a-form-item>
                <a-form-item :label="t('system.config.fieldEncryptAlgorithm')" name="fieldEncrypt.defaultAlgorithm">
                  <a-select v-model:value="cryptoConfig.fieldEncrypt.defaultAlgorithm" :disabled="!cryptoConfig.fieldEncrypt.enabled">
                    <a-select-option value="SM4">SM4（国密标准，默认）</a-select-option>
                    <a-select-option value="AES">AES-256-GCM（国际标准）</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingCrypto" @click="saveCryptoConfig">{{ t('common.save') }}</a-button>
                    <a-button @click="resetCryptoConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <!-- KMS 密钥管理 -->
              <a-form v-if="cryptoSubTab === 'kms'" :model="cryptoConfig" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" layout="horizontal">
                <a-form-item :label="t('system.config.kmsMasterKey')" name="kms.masterKeyHex">
                  <a-input-group compact>
                    <a-input-password v-model:value="cryptoConfig.kms.masterKeyHex" style="width: calc(100% - 120px)" :placeholder="t('system.config.kmsMasterKeyPlaceholder')" />
                    <a-button type="primary" @click="handleGenerateKmsMasterKey">{{ t('system.config.generateKey') }}</a-button>
                  </a-input-group>
                  <a-alert type="error" :message="t('system.config.kmsMasterKeyWarning')" style="margin-top: 8px" show-icon />
                </a-form-item>
                <a-form-item :label="t('system.config.kmsRotateRemindDays')" name="kms.rotateRemindDays">
                  <a-input-number v-model:value="cryptoConfig.kms.rotateRemindDays" :min="7" :max="365" :style="{ width: '100%' }" />
                </a-form-item>
                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingCrypto" @click="saveCryptoConfig">{{ t('common.save') }}</a-button>
                    <a-button @click="resetCryptoConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <!-- TDE 状态检测 -->
              <a-form v-if="cryptoSubTab === 'tde'" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" layout="horizontal">
                <a-form-item :label="t('system.config.tdeEnabled')">
                  <a-tag :color="tdeStatus?.enabled ? 'green' : 'red'">
                    {{ tdeStatus?.enabled ? t('system.config.tdeEnabledYes') : t('system.config.tdeEnabledNo') }}
                  </a-tag>
                  <a-button size="small" @click="handleCheckTde" :loading="checkingTde" style="margin-left: 8px">{{ t('system.config.tdeCheck') }}</a-button>
                </a-form-item>
                <template v-if="tdeStatus?.enabled">
                  <a-form-item :label="t('system.config.tdeEncryptedTables')">
                    <a-tag v-for="table in tdeStatus.encryptedTables" :key="table" color="blue">{{ table }}</a-tag>
                    <span v-if="!tdeStatus.encryptedTables?.length">{{ t('system.config.tdeNoEncryptedTables') }}</span>
                  </a-form-item>
                  <a-form-item :label="t('system.config.tdeKeyringPlugin')">
                    {{ tdeStatus.keyringPlugin || t('system.config.tdeKeyringNotDetected') }}
                  </a-form-item>
                </template>
                <a-alert type="info" style="margin-bottom: 16px">
                  <template #message>{{ t('system.config.tdeInfoMessage') }}</template>
                </a-alert>
              </a-form>
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-modal v-model:open="previewVisible" :title="t('system.config.previewTitle')" width="800px" :footer="null">
      <div class="preview-container">
        <div class="preview-login-page" :style="{ backgroundColor: basicConfig.loginBackgroundColor }">
          <video
            v-if="basicConfig.loginBackgroundType === 'video' && basicConfig.loginBackgroundVideo"
            class="preview-bg-video"
            autoplay
            muted
            loop
            playsinline
            :src="formatMediaUrl(basicConfig.loginBackgroundVideo)"
          ></video>
          <img
            v-if="basicConfig.loginBackgroundType === 'image' && basicConfig.loginBackgroundImage"
            class="preview-bg-image"
            :src="formatMediaUrl(basicConfig.loginBackgroundImage)"
            alt="background"
          />
          <div class="preview-content">
            <div class="preview-brand">
              <img
                v-if="formatMediaUrl(basicConfig.systemLogo)"
                :src="formatMediaUrl(basicConfig.systemLogo)"
                class="preview-logo"
                alt="logo"
              />
              <span v-else class="preview-system-name">{{ basicConfig.systemName }}</span>
            </div>
            <div class="preview-title">{{ basicConfig.loginPageTitle }}</div>
            <div class="preview-subtitle">{{ basicConfig.loginPageSubtitle }}</div>
            <div class="preview-copyright">{{ basicConfig.copyright }}</div>
          </div>
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model:open="folderPickerVisible"
      :title="t('system.config.selectUploadFolder')"
      width="720px"
      :confirm-loading="folderPickerLoading"
      @ok="confirmFolderSelection"
      @cancel="folderPickerVisible = false"
    >
      <div class="folder-picker">
        <div class="folder-picker-toolbar">
          <a-input
            v-model:value="newFolderName"
            :placeholder="t('system.config.newFolderNamePlaceholder')"
            @pressEnter="handleCreateFolder"
          />
          <a-button type="primary" :disabled="!selectedFolderPath" @click="handleCreateFolder">
            {{ t('system.config.createFolder') }}
          </a-button>
        </div>
        <a-tree
          v-model:expandedKeys="folderExpandedKeys"
          v-model:selectedKeys="folderSelectedKeys"
          :tree-data="folderTreeData"
          :load-data="loadFolderChildren"
          :field-names="{ title: 'name', key: 'path', children: 'children' }"
          @select="handleFolderSelect"
        >
          <template #title="{ name, path, writable }">
            <span class="folder-node-title">
              <span>{{ name }}</span>
              <a-tag v-if="writable" color="green">{{ t('system.config.folderWritable') }}</a-tag>
              <a-tag v-else color="default">{{ t('system.config.folderReadonly') }}</a-tag>
              <span class="folder-node-path">{{ path }}</span>
            </span>
          </template>
        </a-tree>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { UploadFile } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  AuditOutlined,
  DashboardOutlined,
  DatabaseOutlined,
  DeleteOutlined,
  FileProtectOutlined,
  FolderOpenOutlined,
  KeyOutlined,
  PictureOutlined,
  SettingOutlined,
  UploadOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import AvatarUpload from '@/components/AvatarUpload.vue'
import ModuleHomepageDesigner from '@/components/module-homepage/ModuleHomepageDesigner.vue'
import PersonalHomepageDesigner from '@/components/personal-homepage/PersonalHomepageDesigner.vue'
import { uploadFile } from '@/api/system/file'
import {
  createDefaultEmailConfig,
  createDefaultFileUploadConfig,
  createDefaultSecurityConfig,
  createDefaultSystemBasicConfig,
  createDefaultCryptoConfig,
  getEmailConfig,
  getFileUploadConfig,
  getSecurityConfig,
  getSystemBasicConfig,
  getCryptoConfig,
  setEmailConfig,
  setFileUploadConfig,
  setSecurityConfig,
  setSystemBasicConfig,
  setCryptoConfig,
  generateSm4Key,
  generateAesKey,
  generateRsaKeyPair,
  generateKmsMasterKey,
  getTdeStatus,
  getFileUploadRuntimeDefaults,
  getFileUploadFolderRoots,
  getFileUploadFolderChildren,
  createFileUploadFolder,
  type EmailConfig,
  type FileUploadConfig,
  type FileUploadFolderNode,
  type FileUploadRuntimeDefaults,
  type SecurityConfig,
  type SystemBasicConfig,
  type CryptoConfig,
  type TdeStatus,
} from '@/api/system/config'
import { normalizeMediaUrl } from '@/utils/media'

const { t } = useI18n()

const activeTab = ref('system')
const securitySubTab = ref('captcha')
const cryptoSubTab = ref('symmetric')
const activeHomepageModule = ref<'personal' | 'basic' | 'approval' | 'sys'>('personal')
const loading = ref(false)
const previewVisible = ref(false)

const savingSystem = ref(false)
const savingPortal = ref(false)
const savingSecurity = ref(false)
const savingEmail = ref(false)
const savingUpload = ref(false)
const savingCrypto = ref(false)
const checkingTde = ref(false)
const videoUploading = ref(false)
const bgImageUploading = ref(false)

const videoFileList = ref<UploadFile[]>([])
const bgImageFileList = ref<UploadFile[]>([])

const basicConfig = ref<SystemBasicConfig>(createDefaultSystemBasicConfig())
const securityConfig = ref<SecurityConfig>(createDefaultSecurityConfig())
const emailConfig = ref<EmailConfig>(createDefaultEmailConfig())
const fileUploadConfig = ref<FileUploadConfig>(createDefaultFileUploadConfig())
const cryptoConfig = ref<CryptoConfig>(createDefaultCryptoConfig())
const tdeStatus = ref<TdeStatus | null>(null)
const runtimeDefaults = ref<FileUploadRuntimeDefaults | null>(null)
const folderPickerVisible = ref(false)
const folderPickerLoading = ref(false)
const folderTreeData = ref<FolderTreeNode[]>([])
const folderExpandedKeys = ref<string[]>([])
const folderSelectedKeys = ref<string[]>([])
const selectedFolderPath = ref('')
const newFolderName = ref('')

type StorageType = FileUploadConfig['storageType']

type ProviderConfig = Record<string, string>

type FolderTreeNode = FileUploadFolderNode & {
  key: string
  isLeaf: boolean
  children?: FolderTreeNode[]
}

const minioConfig = ref<ProviderConfig>({
  endpoint: '',
  accessKey: '',
  secretKey: '',
  bucketName: '',
  domain: '',
})

const ossConfig = ref<ProviderConfig>({
  endpoint: '',
  accessKeyId: '',
  accessKeySecret: '',
  bucketName: '',
  domain: '',
})

type EmailProviderPreset = 'local' | 'aliyun' | 'qq'

const emailProviderOptions: Array<{
  value: EmailProviderPreset
  titleKey: string
  descKey: string
}> = [
  {
    value: 'local',
    titleKey: 'system.config.providerLocalTitle',
    descKey: 'system.config.providerLocalDesc',
  },
  {
    value: 'aliyun',
    titleKey: 'system.config.providerAliyunTitle',
    descKey: 'system.config.providerAliyunDesc',
  },
  {
    value: 'qq',
    titleKey: 'system.config.providerQqTitle',
    descKey: 'system.config.providerQqDesc',
  },
]

const emailProviderPresets: Record<EmailProviderPreset, Partial<EmailConfig>> = {
  local: {
    providerType: 'local',
    smtpHost: '',
    smtpPort: 465,
    authEnabled: true,
    sslEnabled: true,
    starttlsEnabled: true,
  },
  aliyun: {
    providerType: 'aliyun',
    smtpHost: 'smtp.qiye.aliyun.com',
    smtpPort: 465,
    authEnabled: true,
    sslEnabled: true,
    starttlsEnabled: true,
  },
  qq: {
    providerType: 'qq',
    smtpHost: 'smtp.qq.com',
    smtpPort: 465,
    authEnabled: true,
    sslEnabled: true,
    starttlsEnabled: false,
  },
}

type NavOption = { value: string; titleKey: string; descKey: string }

const homepageModuleCards = [
  {
    value: 'personal',
    title: '个人首页',
    desc: '个人工作台默认布局',
    icon: UserOutlined,
  },
  {
    value: 'basic',
    title: '基础信息',
    desc: '供应商与编码规则首页',
    icon: DatabaseOutlined,
  },
  {
    value: 'approval',
    title: '审批管理',
    desc: '审批工作台首页',
    icon: AuditOutlined,
  },
  {
    value: 'sys',
    title: '系统管理',
    desc: '系统运行与配置首页',
    icon: SettingOutlined,
  },
] as const

const activeHomepageModuleCard = computed(() => {
  return homepageModuleCards.find(item => item.value === activeHomepageModule.value) || homepageModuleCards[0]
})

const securityNavOptions: NavOption[] = [
  { value: 'captcha', titleKey: 'system.config.captchaConfig', descKey: 'system.config.captchaConfigDesc' },
  { value: 'password', titleKey: 'system.config.passwordPolicy', descKey: 'system.config.passwordPolicyDesc' },
  { value: 'loginFailure', titleKey: 'system.config.loginSecurity', descKey: 'system.config.loginSecurityDesc' },
  { value: 'transport', titleKey: 'system.config.transportCrypto', descKey: 'system.config.transportCryptoDesc' },
]

const uploadNavOptions: Array<NavOption & { value: StorageType; icon: any }> = [
  { value: 'LOCAL', titleKey: 'system.config.storageLocal', descKey: 'system.config.storageLocalDesc', icon: FolderOpenOutlined },
  { value: 'MINIO', titleKey: 'system.config.storageMinio', descKey: 'system.config.storageMinioDesc', icon: DatabaseOutlined },
  { value: 'OSS', titleKey: 'system.config.storageOss', descKey: 'system.config.storageOssDesc', icon: UploadOutlined },
]

const cryptoNavOptions: NavOption[] = [
  { value: 'symmetric', titleKey: 'system.config.symmetricKeys', descKey: 'system.config.symmetricKeysDesc' },
  { value: 'asymmetric', titleKey: 'system.config.asymmetricKeys', descKey: 'system.config.asymmetricKeysDesc' },
  { value: 'dataEncrypt', titleKey: 'system.config.dataEncryptTab', descKey: 'system.config.dataEncryptDesc' },
  { value: 'kms', titleKey: 'system.config.kmsConfig', descKey: 'system.config.kmsConfigDesc' },
  { value: 'tde', titleKey: 'system.config.tdeStatus', descKey: 'system.config.tdeStatusDesc' },
]

const currentSecurityNav = computed(() => securityNavOptions.find(o => o.value === securitySubTab.value) || securityNavOptions[0])
const currentUploadNav = computed(() => uploadNavOptions.find(o => o.value === fileUploadConfig.value.storageType) || uploadNavOptions[0])
const currentCryptoNav = computed(() => cryptoNavOptions.find(o => o.value === cryptoSubTab.value) || cryptoNavOptions[0])
const fileUploadPreviewUrl = computed(() => {
  const base = normalizePublicBaseUrl(fileUploadConfig.value.publicBaseUrl || runtimeDefaults.value?.recommendedPublicBaseUrl || '')
  const prefix = normalizeAccessPrefix(fileUploadConfig.value.accessPrefix || runtimeDefaults.value?.accessPrefix || '/files')
  return base ? `${base}${prefix}/example.png` : `${prefix}/example.png`
})

function normalizeSystemBasicConfig(config: Partial<SystemBasicConfig> | null | undefined): SystemBasicConfig {
  const defaults = createDefaultSystemBasicConfig()
  return {
    ...defaults,
    ...(config || {}),
    loginLayout: (config?.loginLayout || defaults.loginLayout) as SystemBasicConfig['loginLayout'],
  }
}

function normalizeSecurityConfig(config: Partial<SecurityConfig> | null | undefined): SecurityConfig {
  const defaults = createDefaultSecurityConfig()
  return {
    captcha: {
      ...defaults.captcha,
      ...(config?.captcha || {}),
      image: {
        ...defaults.captcha.image,
        ...(config?.captcha?.image || {}),
      },
      slider: {
        ...defaults.captcha.slider,
        ...(config?.captcha?.slider || {}),
      },
    },
    passwordPolicy: {
      ...defaults.passwordPolicy,
      ...(config?.passwordPolicy || {}),
    },
    loginSecurity: {
      ...defaults.loginSecurity,
      ...(config?.loginSecurity || {}),
    },
    cryptoTransport: {
      ...defaults.cryptoTransport,
      ...(config?.cryptoTransport || {}),
    },
  }
}

function normalizeEmailConfig(config: Partial<EmailConfig> | null | undefined): EmailConfig {
  const defaults = createDefaultEmailConfig()
  return {
    ...defaults,
    ...(config || {}),
    providerType: (config?.providerType || defaults.providerType) as EmailConfig['providerType'],
    smtpPort: typeof config?.smtpPort === 'number' ? config.smtpPort : defaults.smtpPort,
  }
}

function normalizeFileUploadConfig(config: Partial<FileUploadConfig> | null | undefined): FileUploadConfig {
  const defaults = createDefaultFileUploadConfig()
  const publicBaseUrl = normalizePublicBaseUrl(config?.publicBaseUrl || defaults.publicBaseUrl || '')
  const accessPrefix = String(config?.accessPrefix || defaults.accessPrefix || '/files').trim()
  return {
    ...defaults,
    ...(config || {}),
    storageType: ((config?.storageType || defaults.storageType) as FileUploadConfig['storageType']),
    publicBaseUrl,
    accessPrefix: accessPrefix.startsWith('/') ? accessPrefix : `/${accessPrefix}`,
  }
}

function normalizeSystemBasicMedia(config: SystemBasicConfig): SystemBasicConfig {
  return {
    ...config,
    systemLogo: normalizeConfigMediaUrl(config.systemLogo),
    loginBackgroundImage: normalizeConfigMediaUrl(config.loginBackgroundImage),
    loginBackgroundVideo: normalizeConfigMediaUrl(config.loginBackgroundVideo),
  }
}

function normalizeConfigMediaUrl(value: string): string {
  const rawValue = String(value || '').trim().replace(/\\/g, '/')
  if (!rawValue) {
    return ''
  }
  if (
    rawValue.startsWith('data:') ||
    rawValue.startsWith('blob:') ||
    rawValue.startsWith('http://') ||
    rawValue.startsWith('https://') ||
    rawValue.startsWith('//')
  ) {
    return rawValue
  }

  const recommendedBaseUrl = normalizePublicBaseUrl(
    fileUploadConfig.value.publicBaseUrl || runtimeDefaults.value?.recommendedPublicBaseUrl || '',
  )
  if (!recommendedBaseUrl) {
    return rawValue
  }

  if (rawValue.startsWith('/api/files/')) {
    return `${recommendedBaseUrl}${rawValue.substring('/api'.length)}`
  }
  if (rawValue.startsWith('api/files/')) {
    return `${recommendedBaseUrl}/${rawValue.substring('api/'.length)}`
  }
  if (rawValue.startsWith('/files/')) {
    return `${recommendedBaseUrl}${rawValue}`
  }
  if (rawValue.startsWith('files/')) {
    return `${recommendedBaseUrl}/${rawValue}`
  }
  return rawValue
}

function normalizeAccessPrefix(value: string): string {
  const normalized = String(value || '/files').trim().replace(/\/+$/, '') || '/files'
  return normalized.startsWith('/') ? normalized : `/${normalized}`
}

function normalizePublicBaseUrl(value: string): string {
  let rawValue = String(value || '').trim().replace(/\/+$/, '')
  if (!rawValue) {
    return ''
  }
  if (!/^https?:\/\//i.test(rawValue)) {
    rawValue = `http://${rawValue}`
  }

  try {
    const url = new URL(rawValue)
    const recommended = new URL(runtimeDefaults.value?.recommendedPublicBaseUrl || 'http://127.0.0.1:9000/api')
    const gatewayPort = recommended.port || '9000'
    const gatewayPrefix = (recommended.pathname || '/api').replace(/\/+$/, '') || '/api'
    const port = url.port || gatewayPort
    const inputPath = (url.pathname === '/' ? '' : (url.pathname || '').replace(/\/+$/, ''))
    const shouldUseGatewayPrefix = port === gatewayPort
      && inputPath !== gatewayPrefix
      && !inputPath.startsWith(`${gatewayPrefix}/`)
    const path = shouldUseGatewayPrefix
      ? `${gatewayPrefix}${inputPath === '/' ? '' : inputPath}`
      : (inputPath === '/' ? '' : inputPath)

    return `${url.protocol}//${url.hostname}${port ? `:${port}` : ''}${path}`
  } catch (e) {
    return rawValue
  }
}

function parseProviderConfig(value: string): ProviderConfig {
  if (!value.trim()) {
    return {}
  }
  try {
    const parsed = JSON.parse(value)
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
  } catch (e) {
    return {}
  }
}

function stringifyProviderConfig(config: ProviderConfig): string {
  const normalized = Object.entries(config).reduce<ProviderConfig>((acc, [key, value]) => {
    const text = String(value || '').trim()
    if (text) {
      acc[key] = text
    }
    return acc
  }, {})
  return JSON.stringify(normalized, null, 2)
}

function syncProviderForms(config: FileUploadConfig) {
  const parsed = parseProviderConfig(config.providerConfigJson || '')
  minioConfig.value = {
    endpoint: parsed.endpoint || '',
    accessKey: parsed.accessKey || '',
    secretKey: parsed.secretKey || '',
    bucketName: parsed.bucketName || '',
    domain: parsed.domain || '',
  }
  ossConfig.value = {
    endpoint: parsed.endpoint || '',
    accessKeyId: parsed.accessKeyId || '',
    accessKeySecret: parsed.accessKeySecret || '',
    bucketName: parsed.bucketName || '',
    domain: parsed.domain || '',
  }
}

function toFolderTreeNode(node: FileUploadFolderNode): FolderTreeNode {
  return {
    ...node,
    key: node.path,
    isLeaf: node.leaf,
    children: node.leaf ? [] : undefined,
  }
}

function normalizeCryptoConfig(config: Partial<CryptoConfig> | null | undefined): CryptoConfig {
  const defaults = createDefaultCryptoConfig()
  return {
    sm4: { ...defaults.sm4, ...(config?.sm4 || {}) },
    aes: { ...defaults.aes, ...(config?.aes || {}) },
    rsa: { ...defaults.rsa, ...(config?.rsa || {}) },
    kms: { ...defaults.kms, ...(config?.kms || {}) },
    fileEncrypt: { ...defaults.fileEncrypt, ...(config?.fileEncrypt || {}) },
    fieldEncrypt: { ...defaults.fieldEncrypt, ...(config?.fieldEncrypt || {}) },
  }
}

function formatMediaUrl(value: string): string {
  return normalizeMediaUrl(value)
}

async function loadAllConfig() {
  loading.value = true
  try {
    const [basic, security, email, upload, crypto, uploadDefaults] = await Promise.all([
      getSystemBasicConfig(),
      getSecurityConfig(),
      getEmailConfig(),
      getFileUploadConfig(),
      getCryptoConfig(),
      getFileUploadRuntimeDefaults(),
    ])
    runtimeDefaults.value = uploadDefaults
    fileUploadConfig.value = normalizeFileUploadConfig(upload)
    basicConfig.value = normalizeSystemBasicMedia(normalizeSystemBasicConfig(basic))
    securityConfig.value = normalizeSecurityConfig(security)
    emailConfig.value = normalizeEmailConfig(email)
    if (fileUploadConfig.value.storageType === 'LOCAL' && !fileUploadConfig.value.publicBaseUrl && uploadDefaults.recommendedPublicBaseUrl) {
      fileUploadConfig.value.publicBaseUrl = uploadDefaults.recommendedPublicBaseUrl
    }
    if (!fileUploadConfig.value.accessPrefix && uploadDefaults.accessPrefix) {
      fileUploadConfig.value.accessPrefix = uploadDefaults.accessPrefix
    }
    basicConfig.value = normalizeSystemBasicMedia(basicConfig.value)
    syncProviderForms(fileUploadConfig.value)
    cryptoConfig.value = normalizeCryptoConfig(crypto)
  } catch (e) {
    message.error(t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function saveSystemConfig() {
  savingSystem.value = true
  try {
    basicConfig.value = normalizeSystemBasicMedia(basicConfig.value)
    await setSystemBasicConfig(basicConfig.value)
    // 成功/失败提示均由后端返回，http 拦截器会自动显示
  } catch (e) {
    // 错误由 http 拦截器统一处理并显示后端返回的错误消息
  } finally {
    savingSystem.value = false
  }
}

async function savePortalConfig() {
  savingPortal.value = true
  try {
    basicConfig.value = normalizeSystemBasicMedia(basicConfig.value)
    await setSystemBasicConfig(basicConfig.value)
    // 成功/失败提示均由后端返回，http 拦截器会自动显示
  } catch (e) {
    // 错误由 http 拦截器统一处理并显示后端返回的错误消息
  } finally {
    savingPortal.value = false
  }
}

async function saveSecurityConfig() {
  savingSecurity.value = true
  try {
    await setSecurityConfig(securityConfig.value)
    // 成功/失败提示均由后端返回，http 拦截器会自动显示
  } catch (e) {
    // 错误由 http 拦截器统一处理并显示后端返回的错误消息
  } finally {
    savingSecurity.value = false
  }
}

async function saveFileUploadConfig() {
  savingUpload.value = true
  try {
    fileUploadConfig.value.accessPrefix = normalizeAccessPrefix(fileUploadConfig.value.accessPrefix)
    fileUploadConfig.value.publicBaseUrl = normalizePublicBaseUrl(fileUploadConfig.value.publicBaseUrl)
    if (fileUploadConfig.value.storageType === 'LOCAL' && !fileUploadConfig.value.publicBaseUrl.trim()) {
      message.error(t('system.config.publicBaseUrlRequired'))
      return
    }
    if (fileUploadConfig.value.storageType === 'LOCAL') {
      fileUploadConfig.value.providerConfigJson = ''
    } else if (fileUploadConfig.value.storageType === 'MINIO') {
      fileUploadConfig.value.providerConfigJson = stringifyProviderConfig(minioConfig.value)
    } else {
      fileUploadConfig.value.providerConfigJson = stringifyProviderConfig(ossConfig.value)
    }
    await setFileUploadConfig(fileUploadConfig.value)
    // 成功/失败提示均由后端返回，http 拦截器会自动显示
  } catch (e) {
    // 错误由 http 拦截器统一处理并显示后端返回的错误消息
  } finally {
    savingUpload.value = false
  }
}

async function saveEmail() {
  savingEmail.value = true
  try {
    await setEmailConfig(normalizeEmailConfig(emailConfig.value))
    // 成功/失败提示均由后端返回，http 拦截器会自动显示
  } catch (e) {
    // 错误由 http 拦截器统一处理并显示后端返回的错误消息
  } finally {
    savingEmail.value = false
  }
}

function resetSystemConfig() {
  const defaults = createDefaultSystemBasicConfig()
  basicConfig.value = {
    ...basicConfig.value,
    systemName: defaults.systemName,
    systemLogo: defaults.systemLogo,
    systemVersion: defaults.systemVersion,
    copyright: defaults.copyright,
    copyrightLink: defaults.copyrightLink,
  }
  message.info(t('common.resetSuccess'))
}

function resetPortalConfig() {
  const defaults = createDefaultSystemBasicConfig()
  basicConfig.value = {
    ...basicConfig.value,
    loginPageTitle: defaults.loginPageTitle,
    loginPageSubtitle: defaults.loginPageSubtitle,
    loginBackgroundType: defaults.loginBackgroundType,
    loginBackgroundVideo: defaults.loginBackgroundVideo,
    loginBackgroundImage: defaults.loginBackgroundImage,
    loginBackgroundColor: defaults.loginBackgroundColor,
    loginStyle: defaults.loginStyle,
    loginLayout: defaults.loginLayout,
    showOAuthLogin: defaults.showOAuthLogin,
    showRegisterEntry: defaults.showRegisterEntry,
    registerUrl: defaults.registerUrl,
    primaryColor: defaults.primaryColor,
    secondaryColor: defaults.secondaryColor,
  }
  videoFileList.value = []
  bgImageFileList.value = []
  message.info(t('common.resetSuccess'))
}

function resetSecurityConfig() {
  securityConfig.value = createDefaultSecurityConfig()
  message.info(t('common.resetSuccess'))
}

function resetEmailConfig() {
  emailConfig.value = createDefaultEmailConfig()
  message.info(t('common.resetSuccess'))
}

function resetFileUploadConfig() {
  fileUploadConfig.value = createDefaultFileUploadConfig()
  if (runtimeDefaults.value?.recommendedPublicBaseUrl) {
    fileUploadConfig.value.publicBaseUrl = runtimeDefaults.value.recommendedPublicBaseUrl
  }
  if (runtimeDefaults.value?.accessPrefix) {
    fileUploadConfig.value.accessPrefix = runtimeDefaults.value.accessPrefix
  }
  syncProviderForms(fileUploadConfig.value)
  message.info(t('common.resetSuccess'))
}

async function saveCryptoConfig() {
  savingCrypto.value = true
  try {
    await setCryptoConfig(cryptoConfig.value)
  } catch (e) {
    // 错误由 http 拦截器统一处理
  } finally {
    savingCrypto.value = false
  }
}

function resetCryptoConfig() {
  cryptoConfig.value = createDefaultCryptoConfig()
  message.info(t('common.resetSuccess'))
}

async function handleGenerateSm4Key() {
  const res = await generateSm4Key()
  cryptoConfig.value.sm4.keyHex = res.keyHex
}

async function handleGenerateAesKey() {
  const res = await generateAesKey()
  cryptoConfig.value.aes.keyHex = res.keyHex
}

async function handleGenerateRsaKeyPair() {
  const res = await generateRsaKeyPair(cryptoConfig.value.rsa.keySize)
  cryptoConfig.value.rsa.publicKey = res.publicKey
  cryptoConfig.value.rsa.privateKey = res.privateKey
}

async function handleGenerateKmsMasterKey() {
  const res = await generateKmsMasterKey()
  cryptoConfig.value.kms.masterKeyHex = res.masterKeyHex
}

async function handleCheckTde() {
  checkingTde.value = true
  try {
    tdeStatus.value = await getTdeStatus()
  } catch (e) {
    message.error(t('system.config.tdeCheckFailed'))
  } finally {
    checkingTde.value = false
  }
}

function handleUploadTypeChange(type: StorageType) {
  fileUploadConfig.value.storageType = type
  if (type === 'LOCAL' && !fileUploadConfig.value.publicBaseUrl && runtimeDefaults.value?.recommendedPublicBaseUrl) {
    fileUploadConfig.value.publicBaseUrl = runtimeDefaults.value.recommendedPublicBaseUrl
  }
  if (type !== 'LOCAL') {
    syncProviderForms(fileUploadConfig.value)
  }
}

function applyRecommendedPublicBaseUrl() {
  if (runtimeDefaults.value?.recommendedPublicBaseUrl) {
    fileUploadConfig.value.publicBaseUrl = runtimeDefaults.value.recommendedPublicBaseUrl
  }
  if (runtimeDefaults.value?.accessPrefix) {
    fileUploadConfig.value.accessPrefix = runtimeDefaults.value.accessPrefix
  }
}

async function openFolderPicker() {
  folderPickerVisible.value = true
  folderPickerLoading.value = true
  try {
    const roots = await getFileUploadFolderRoots()
    folderTreeData.value = roots.map(toFolderTreeNode)
    if (fileUploadConfig.value.localUploadPath) {
      selectedFolderPath.value = fileUploadConfig.value.localUploadPath
      folderSelectedKeys.value = [fileUploadConfig.value.localUploadPath]
    }
  } finally {
    folderPickerLoading.value = false
  }
}

async function loadFolderChildren(treeNode: any) {
  const node = treeNode.dataRef as FolderTreeNode
  if (node.children && node.children.length) {
    return
  }
  const children = await getFileUploadFolderChildren(node.path)
  node.children = children.map(toFolderTreeNode)
  folderTreeData.value = [...folderTreeData.value]
}

function handleFolderSelect(keys: string[]) {
  selectedFolderPath.value = keys[0] || ''
}

function confirmFolderSelection() {
  if (selectedFolderPath.value) {
    fileUploadConfig.value.localUploadPath = selectedFolderPath.value
  }
  folderPickerVisible.value = false
}

async function handleCreateFolder() {
  const parentPath = selectedFolderPath.value
  const folderName = newFolderName.value.trim()
  if (!parentPath || !folderName) {
    return
  }
  folderPickerLoading.value = true
  try {
    const created = await createFileUploadFolder(parentPath, folderName)
    const createdNode = toFolderTreeNode(created)
    selectedFolderPath.value = created.path
    folderSelectedKeys.value = [created.path]
    folderExpandedKeys.value = Array.from(new Set([...folderExpandedKeys.value, parentPath]))
    await refreshFolderNode(parentPath, createdNode)
    newFolderName.value = ''
  } finally {
    folderPickerLoading.value = false
  }
}

async function refreshFolderNode(parentPath: string, selectedChild?: FolderTreeNode) {
  const children = (await getFileUploadFolderChildren(parentPath)).map(toFolderTreeNode)
  const attachChildren = (nodes: FolderTreeNode[]): boolean => {
    for (const node of nodes) {
      if (node.path === parentPath) {
        node.children = children
        return true
      }
      if (node.children && attachChildren(node.children)) {
        return true
      }
    }
    return false
  }
  attachChildren(folderTreeData.value)
  if (selectedChild && !children.some(item => item.path === selectedChild.path)) {
    folderTreeData.value.push(selectedChild)
  }
  folderTreeData.value = [...folderTreeData.value]
}

function handleEmailProviderChange(provider: EmailProviderPreset) {
  const current = emailConfig.value
  const preset = emailProviderPresets[provider]
  emailConfig.value = normalizeEmailConfig({
    ...current,
    ...preset,
    providerType: provider,
    senderAccount: current.senderAccount,
    senderPassword: current.senderPassword,
  })
}

function openPreview() {
  previewVisible.value = true
}

function handleVideoRemove() {
  basicConfig.value.loginBackgroundVideo = ''
  videoFileList.value = []
}

function handleBgImageRemove() {
  basicConfig.value.loginBackgroundImage = ''
  bgImageFileList.value = []
}

function handleVideoBeforeUpload(file: File) {
  const isVideo = file.type.startsWith('video/')
  const isLt200M = file.size / 1024 / 1024 < 200
  if (!isVideo) {
    message.error(t('common.videoOnly'))
    return false
  }
  if (!isLt200M) {
    message.error(t('system.config.videoSizeLimit'))
    return false
  }
  return true
}

async function handleVideoUpload(options: any) {
  videoUploading.value = true
  try {
    const fileUrl = await uploadFile(options.file, {
      moduleCode: 'sys-config',
      moduleName: '系统配置',
    })
    basicConfig.value.loginBackgroundVideo = normalizeConfigMediaUrl(fileUrl)
    videoFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'done', url: fileUrl }]
    options.onSuccess?.(fileUrl)
  } catch (e) {
    videoFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'error' }]
    options.onError?.(e)
    message.error(t('common.uploadFailed'))
  } finally {
    videoUploading.value = false
  }
}

function handleBgImageBeforeUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    message.error(t('common.imageOnly'))
    return false
  }
  if (!isLt5M) {
    message.error(t('common.sizeLimit'))
    return false
  }
  return true
}

async function handleBgImageUpload(options: any) {
  bgImageUploading.value = true
  try {
    const fileUrl = await uploadFile(options.file, {
      moduleCode: 'sys-config',
      moduleName: '系统配置',
    })
    basicConfig.value.loginBackgroundImage = normalizeConfigMediaUrl(fileUrl)
    bgImageFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'done', url: fileUrl }]
    options.onSuccess?.(fileUrl)
  } catch (e) {
    bgImageFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'error' }]
    options.onError?.(e)
    message.error(t('common.uploadFailed'))
  } finally {
    bgImageUploading.value = false
  }
}

function handleLogoUploadSuccess(url?: string) {
  basicConfig.value.systemLogo = normalizeConfigMediaUrl(url || basicConfig.value.systemLogo)
  message.success(t('common.uploadSuccess'))
}

onMounted(() => {
  loadAllConfig()
})
</script>

<style scoped>
.system-config {
  padding: 0;
  height: 100%;
  min-height: 0;
  overflow: auto;
}

.form-item-hint {
  font-size: 12px;
  color: var(--fx-text-secondary, #999);
  margin-top: 4px;
}

.system-logo-upload {
  display: inline-block;
}

.system-logo-upload :deep(.avatar-upload) {
  align-items: flex-start;
}

.system-logo-upload :deep(.avatar-upload .avatar-uploader .ant-upload) {
  width: 160px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}

.system-logo-upload :deep(.avatar-upload .avatar-image) {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: var(--fx-fill-alter, #f9fafb);
}

.system-logo-upload :deep(.avatar-upload .upload-tips) {
  margin-top: 8px;
  text-align: left;
}

.media-preview {
  margin-top: 8px;
}

.session-timeout-alert {
  margin-bottom: 16px;
}

/* ===== 通用侧边栏配置布局（安全验证 / 加密配置复用） ===== */
.sidebar-config-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 20px;
}

.sidebar-nav-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar-nav-card {
  border: 1px solid var(--fx-border-color, rgba(148, 163, 184, 0.2));
  border-radius: 16px;
  background: var(--fx-bg-container);
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: var(--fx-shadow, 0 2px 8px rgba(0, 0, 0, 0.04));
}

.sidebar-nav-card:hover,
.sidebar-nav-card--active {
  border-color: var(--fx-primary);
  transform: translateY(-1px);
  box-shadow: var(--fx-shadow-secondary, 0 4px 12px rgba(22, 119, 255, 0.15));
}

.sidebar-nav-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--fx-text-primary);
}

.sidebar-nav-card__heading {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.sidebar-nav-card__icon {
  color: var(--fx-primary);
  font-size: 16px;
}

.sidebar-nav-card__desc {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--fx-text-secondary);
}

.inline-link-button {
  height: auto;
  padding: 0 0 0 8px;
}

.upload-url-preview {
  margin: 0 16px 24px 25%;
}

.folder-picker {
  min-height: 360px;
}

.folder-picker-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  margin-bottom: 12px;
}

.folder-node-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.folder-node-path {
  color: var(--fx-text-secondary);
  font-size: 12px;
}

.sidebar-config-main {
  border: 1px solid var(--fx-border-color);
  border-radius: 20px;
  padding: 24px 16px 8px;
  background: var(--fx-bg-container);
}

.homepage-config-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.homepage-module-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.homepage-module-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
  padding: 14px;
  border: 1px solid var(--fx-border-color, rgba(148, 163, 184, 0.2));
  border-radius: 8px;
  background: var(--fx-bg-container);
  color: var(--fx-text-primary);
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.homepage-module-card:hover,
.homepage-module-card--active {
  border-color: var(--fx-primary);
  box-shadow: var(--fx-shadow-secondary, 0 4px 12px rgba(22, 119, 255, 0.15));
}

.homepage-module-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: color-mix(in srgb, var(--fx-primary, #1677ff) 10%, transparent);
  color: var(--fx-primary, #1677ff);
  flex: 0 0 auto;
}

.homepage-module-card__body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.homepage-module-card__title {
  font-size: 15px;
  font-weight: 600;
}

.homepage-module-card__desc {
  font-size: 12px;
  line-height: 1.5;
  color: var(--fx-text-secondary);
}

.homepage-config-main {
  min-width: 0;
  border: 1px solid var(--fx-border-color);
  border-radius: 8px;
  background: var(--fx-bg-container);
  overflow: hidden;
}

.homepage-config-main :deep(.personal-homepage-designer),
.homepage-config-main :deep(.module-homepage-designer) {
  padding: 16px;
}

.sidebar-config-banner {
  margin: 0 16px 24px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--fx-primary) 0%, var(--fx-primary-hover) 100%);
  color: #fff;
}

.sidebar-config-banner__label {
  font-size: 13px;
  opacity: 0.78;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.sidebar-config-banner__text {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
}

.video-preview {
  max-width: 300px;
  max-height: 200px;
}

.color-input {
  width: 50px;
}

.email-config-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 20px;
}

.email-provider-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.email-provider-card {
  border: 1px solid var(--fx-border-color, rgba(148, 163, 184, 0.2));
  border-radius: 16px;
  background: var(--fx-bg-container);
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: var(--fx-shadow, 0 2px 8px rgba(0, 0, 0, 0.04));
}

.email-provider-card:hover,
.email-provider-card--active {
  border-color: var(--fx-primary);
  transform: translateY(-1px);
  box-shadow: var(--fx-shadow-secondary, 0 4px 12px rgba(22, 119, 255, 0.15));
}

.email-provider-card__title {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--fx-text-primary);
}

.email-provider-card__desc {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--fx-text-secondary);
}

.email-config-main {
  border: 1px solid var(--fx-border-color);
  border-radius: 20px;
  padding: 24px 16px 8px;
  background: var(--fx-bg-container);
}

.email-config-main :deep(.ant-input),
.email-config-main :deep(.ant-input-password) {
  background: var(--fx-bg-container) !important;
  border-color: var(--fx-border-color);
  color: var(--fx-text-primary);
  -webkit-text-fill-color: var(--fx-text-primary);
}

.email-config-main :deep(.ant-input:hover),
.email-config-main :deep(.ant-input-password:hover) {
  border-color: var(--fx-primary);
}

.email-config-main :deep(.ant-input:focus),
.email-config-main :deep(.ant-input-password:focus) {
  border-color: var(--fx-primary);
  box-shadow: 0 0 0 2px var(--fx-primary-bg, rgba(22, 119, 255, 0.1));
}

.email-config-main :deep(.ant-input:-webkit-autofill),
.email-config-main :deep(.ant-input-password:-webkit-autofill) {
  -webkit-box-shadow: 0 0 0 1000px var(--fx-bg-container) inset !important;
  box-shadow: 0 0 0 1000px var(--fx-bg-container) inset !important;
  -webkit-text-fill-color: var(--fx-text-primary) !important;
  color: var(--fx-text-primary) !important;
  background-color: transparent !important;
  caret-color: var(--fx-text-primary) !important;
}

.email-config-main :deep(.ant-input-number) {
  background: var(--fx-bg-container) !important;
  border-color: var(--fx-border-color);
  color: var(--fx-text-primary);
  width: 100%;
  -webkit-text-fill-color: var(--fx-text-primary);
}

.email-config-main :deep(.ant-input-number:hover) {
  border-color: var(--fx-primary);
}

.email-config-main :deep(.ant-input-number:focus) {
  border-color: var(--fx-primary);
  box-shadow: 0 0 0 2px var(--fx-primary-bg, rgba(22, 119, 255, 0.1));
}

.email-config-main :deep(.ant-input-number:-webkit-autofill) {
  -webkit-box-shadow: 0 0 0 1000px var(--fx-bg-container) inset !important;
  box-shadow: 0 0 0 1000px var(--fx-bg-container) inset !important;
  -webkit-text-fill-color: var(--fx-text-primary) !important;
  color: var(--fx-text-primary) !important;
  background-color: transparent !important;
  caret-color: var(--fx-text-primary) !important;
}

.email-config-main :deep(.ant-form-item-label > label) {
  color: var(--fx-text-primary);
}

.email-config-banner {
  margin: 0 16px 24px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--fx-primary) 0%, var(--fx-primary-hover) 100%);
  color: #fff;
}

.email-config-banner__label {
  font-size: 13px;
  opacity: 0.78;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.email-config-banner__text {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
}

.preview-container {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.preview-login-page {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: hidden;
}

.preview-bg-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-bg-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 24px;
  background: linear-gradient(180deg, rgba(13, 2, 33, 0.7), rgba(13, 2, 33, 0.9));
}

.preview-brand {
  font-family: 'Orbitron', sans-serif;
  font-weight: 700;
  color: #fff;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-logo {
  width: 80px;
  height: 80px;
  object-fit: cover;
}

.preview-system-name {
  font-size: 24px;
  text-shadow: 0 0 6px #05d9e8, 0 0 12px #05d9e8;
}

.preview-title {
  color: #9ca3af;
  margin-top: 6px;
  margin-bottom: 18px;
  font-size: 14px;
}

.preview-subtitle {
  color: #9ca3af;
  margin-bottom: 24px;
  font-size: 14px;
}

.preview-copyright {
  position: absolute;
  bottom: 16px;
  left: 0;
  right: 0;
  text-align: center;
  color: #9ca3af;
  font-size: 12px;
}

@media (max-width: 960px) {
  .homepage-config-layout,
  .email-config-layout,
  .sidebar-config-layout {
    grid-template-columns: 1fr;
  }

  .homepage-module-list,
  .email-provider-list,
  .sidebar-nav-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  }

  .upload-url-preview {
    margin-left: 0;
  }
}
</style>
